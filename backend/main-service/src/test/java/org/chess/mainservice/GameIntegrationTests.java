package org.chess.mainservice;

import org.chess.mainservice.dto.GameDto;
import org.chess.mainservice.dto.MoveRequest;
import org.chess.mainservice.model.User;
import org.chess.mainservice.repository.*;
import org.chess.mainservice.service.GameService;
import org.chess.mainservice.security.jwt.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameIntegrationTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired GameRepository games;
    @Autowired MoveRepository moves;
    @Autowired GameService service;
    @Autowired JwtService jwt;
    @Autowired PlatformTransactionManager transactions;
    final JsonMapper json = JsonMapper.builder().build();
    final HttpClient http = HttpClient.newHttpClient();
    final List<Long> userIds = new ArrayList<>();
    final List<Long> gameIds = new ArrayList<>();
    final List<SocketClient> sockets = new ArrayList<>();

    @AfterEach
    void cleanup() {
        sockets.forEach(SocketClient::close);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            for (Long id : gameIds) {
                moves.deleteAll(moves.findByGameIdOrderByPlyAsc(id));
                games.deleteById(id);
            }
            games.flush();
            users.deleteAllById(userIds);
        });
    }

    @Test
    void restRegistrationLoginGameFlowAndValidation() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        HttpResponse<String> registration = request("POST", "/api/auth/register", null,
                "{\"username\":\"" + UUID.randomUUID() + "\",\"email\":\"" + email + "\",\"password\":\"StrongPass123\"}");
        assertThat(registration.statusCode()).isEqualTo(201);
        long white = json.readTree(registration.body()).get("id").asLong();
        userIds.add(white);
        var login = request("POST", "/api/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"StrongPass123\"}");
        assertThat(login.statusCode()).isEqualTo(200);
        String token = json.readTree(login.body()).get("token").asText();
        String refreshToken = json.readTree(login.body()).get("refreshToken").asText();
        assertThat(request("GET", "/api/users/me", refreshToken, null).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/api/auth/refresh", null,
                "{\"refreshToken\":\"" + token + "\"}").statusCode()).isEqualTo(401);
        assertThat(request("POST", "/api/games", null, "{}").statusCode()).isEqualTo(401);
        var created = request("POST", "/api/games", token, "{}");
        assertThat(created.statusCode()).isEqualTo(201);
        long id = json.readTree(created.body()).get("id").asLong();
        gameIds.add(id);
        assertThat(created.body()).doesNotContain("password", "email");
        assertThat(request("GET", "/api/games", token, null).body()).contains("\"id\":" + id);
        assertThat(request("POST", "/api/games/" + id + "/join", token, "{}").statusCode()).isEqualTo(400);
        User black = user();
        String blackToken = token(black);
        assertThat(request("GET", "/api/users/" + white, blackToken, null).statusCode()).isEqualTo(403);
        assertThat(request("PUT", "/api/users/" + white, blackToken,
                "{\"username\":\"hijacked\",\"email\":\"hijacked@example.com\"}").statusCode()).isEqualTo(403);
        assertThat(request("GET", "/api/users", token, null).statusCode()).isEqualTo(404);
        assertThat(request("POST", "/api/games/" + id + "/join", blackToken, "{}").statusCode()).isEqualTo(200);
        assertThat(request("POST", "/api/games/" + id + "/moves", token, "{\"from\":\"z9\",\"to\":\"e4\"}")
                .statusCode()).isEqualTo(400);
        assertThat(request("POST", "/api/games/" + id + "/moves", token, "{\"from\":\"e2\",\"to\":\"e4\"}")
                .statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/games/" + id, token(user()), null).statusCode()).isEqualTo(403);
        assertThat(request("POST", "/api/games/" + id + "/resign", blackToken, "{}").body()).contains("WHITE_WIN");
        assertThat(request("GET", "/api/games/my", token, null).body()).contains("FINISHED");
    }

    @Test
    void websocketBroadcastsCommittedStateSendsPrivateErrorsAndReconnects() throws Exception {
        User white = user();
        User black = user();
        long id = create(white);
        SocketClient first = connect(token(white));
        first.subscribe(id);
        first.sendMove(id, "z9", "e4");
        assertThat(first.next("MESSAGE").body()).contains("Invalid move format");
        first.sendMove(id, "e2", "e4");
        assertThat(first.next("MESSAGE").body()).contains("Game is not in progress");

        service.join(id, black.getId());
        assertThat(first.next("MESSAGE").body()).contains("PLAYER_JOINED");
        SocketClient second = connect(token(black));
        second.subscribe(id);
        second.sendMove(id, "e7", "e5");
        assertThat(second.next("MESSAGE").body()).contains("It is not your turn");
        assertThat(first.frames.poll(200, TimeUnit.MILLISECONDS)).isNull();

        first.sendMove(id, "e2", "e4");
        Frame whiteMove = first.next("MESSAGE");
        Frame blackMove = second.next("MESSAGE");
        assertThat(whiteMove.body()).contains("\"type\":\"MOVE\"", "\"san\":\"e4\"");
        assertThat(blackMove.body()).isEqualTo(whiteMove.body());
        assertThat(service.get(id, black.getId()).moves()).hasSize(1);

        second.close();
        SocketClient reconnected = connect(token(black));
        reconnected.subscribe(id);
        // Fresh HTTP snapshot after reconnect must include the committed move.
        assertThat(request("GET", "/api/games/" + id, token(black), null).body()).contains("\"uci\":\"e2e4\"");
        reconnected.sendMove(id, "e7", "e5");
        assertThat(reconnected.next("MESSAGE").body()).contains("\"uci\":\"e7e5\"");
        first.next("MESSAGE");
        service.resign(id, white.getId());
        assertThat(first.next("MESSAGE").body()).contains("GAME_OVER", "BLACK_WIN");
        assertThat(reconnected.next("MESSAGE").body()).contains("GAME_OVER");
    }

    @Test
    void rejectsUnauthenticatedConnectionsAndForeignSubscriptions() throws Exception {
        SocketClient anonymous = open();
        anonymous.send("CONNECT\naccept-version:1.2\nhost:localhost\n\n");
        assertThat(anonymous.next("ERROR").command()).isEqualTo("ERROR");
        User white = user();
        long id = create(white);
        service.join(id, user().getId());
        SocketClient outsider = connect(token(user()));
        outsider.send("SUBSCRIBE\nid:foreign\ndestination:/topic/games/" + id + "\n\n");
        assertThat(outsider.next("ERROR").command()).isEqualTo("ERROR");

        SocketClient player = connect(token(white));
        player.send("SEND\ndestination:/topic/games/" + id + "\n\n{}");
        assertThat(player.next("ERROR").command()).isEqualTo("ERROR");
    }

    @Test
    void serializesConcurrentJoinsAndMoves() throws Exception {
        User white = user();
        User black = user();
        User other = user();
        long id = create(white);
        List<Boolean> joined = race(() -> service.join(id, black.getId()), () -> service.join(id, other.getId()));
        assertThat(joined).containsExactlyInAnyOrder(true, false);
        List<Boolean> played = race(
                () -> service.move(id, white.getId(), new MoveRequest("e2", "e4", null)),
                () -> service.move(id, white.getId(), new MoveRequest("d2", "d4", null)));
        assertThat(played).containsExactlyInAnyOrder(true, false);
        GameDto state = service.get(id, white.getId());
        assertThat(state.moves()).hasSize(1);
        assertThat(state.currentFen()).isEqualTo(state.moves().getFirst().fenAfter());
    }

    private List<Boolean> race(Callable<?> first, Callable<?> second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (Callable<?> action : List.of(first, second)) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    go.await();
                    try { action.call(); return true; }
                    catch (org.chess.mainservice.exception.GameException expected) { return false; }
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            go.countDown();
            return List.of(results.get(0).get(10, TimeUnit.SECONDS), results.get(1).get(10, TimeUnit.SECONDS));
        }
    }

    private User user() {
        String name = UUID.randomUUID().toString();
        User user = users.save(new User(name, name + "@example.com", "test"));
        userIds.add(user.getId());
        return user;
    }

    private String token(User user) { return jwt.generateAuthToken(user.getEmail()).getToken(); }

    private long create(User white) {
        long id = service.create(white.getId()).id();
        gameIds.add(id);
        return id;
    }

    private HttpResponse<String> request(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return http.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() :
                HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private SocketClient open() {
        SocketClient client = new SocketClient();
        client.socket = http.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create("ws://localhost:" + port + "/ws"), client).join();
        sockets.add(client);
        return client;
    }

    private SocketClient connect(String token) throws Exception {
        SocketClient client = open();
        client.send("CONNECT\naccept-version:1.2\nhost:localhost\nAuthorization:Bearer " + token + "\n\n");
        client.next("CONNECTED");
        return client;
    }

    record Frame(String command, String body) {}

    static class SocketClient implements WebSocket.Listener {
        final BlockingQueue<Frame> frames = new LinkedBlockingQueue<>();
        final StringBuilder buffer = new StringBuilder();
        WebSocket socket;

        @Override
        public CompletionStage<?> onBinary(WebSocket socket, java.nio.ByteBuffer data, boolean last) {
            return onText(socket, java.nio.charset.StandardCharsets.UTF_8.decode(data), last);
        }

        @Override
        public void onError(WebSocket socket, Throwable error) {
            frames.add(new Frame("CLIENT_ERROR", error.toString()));
        }

        @Override
        public CompletionStage<?> onClose(WebSocket socket, int status, String reason) {
            frames.add(new Frame("CLOSED", status + " " + reason));
            return null;
        }

        @Override
        public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
            buffer.append(data);
            int end;
            while ((end = buffer.indexOf("\0")) >= 0) {
                String frame = buffer.substring(0, end).stripLeading();
                buffer.delete(0, end + 1);
                int separator = frame.indexOf("\n\n");
                frames.add(new Frame(frame.substring(0, frame.indexOf('\n')),
                        separator < 0 ? "" : frame.substring(separator + 2)));
            }
            socket.request(1);
            return null;
        }

        void send(String frame) { socket.sendText(frame + "\0", true).join(); }

        void subscribe(long id) throws Exception {
            send("SUBSCRIBE\nid:errors\ndestination:/user/queue/errors\n\n");
            send("SUBSCRIBE\nid:game\ndestination:/topic/games/" + id + "\n\n");
            send("SUBSCRIBE\nid:state\ndestination:/user/queue/game-state\n\n");
            send("SEND\ndestination:/app/games/" + id + "/sync\n\n{}");
            assertThat(next("MESSAGE").body()).contains("\"id\":" + id);
        }

        void sendMove(long id, String from, String to) {
            send("SEND\ndestination:/app/games/" + id + "/move\ncontent-type:application/json\n\n"
                    + "{\"from\":\"" + from + "\",\"to\":\"" + to + "\"}");
        }

        Frame next(String command) throws Exception {
            Frame frame = frames.poll(10, TimeUnit.SECONDS);
            assertThat(frame).as("Expected STOMP %s", command).isNotNull();
            assertThat(frame.command()).isEqualTo(command);
            return frame;
        }

        void close() { if (socket != null) socket.abort(); }
    }
}
