package org.chess.mainservice.security;

import org.chess.mainservice.security.jwt.JwtService;
import org.chess.mainservice.service.GameService;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {
    private static final Pattern GAME_TOPIC = Pattern.compile("/topic/games/([0-9]+)");
    private static final Pattern GAME_DESTINATION = Pattern.compile("/app/games/[0-9]+/(move|sync)");
    private final JwtService jwt;
    private final CustomUserDetailsService users;
    private final GameService games;

    public StompAuthInterceptor(JwtService jwt, CustomUserDetailsService users, GameService games) {
        this.jwt = jwt;
        this.users = users;
        this.games = games;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null || headers.getCommand() == null) {
            return message;
        }
        if (headers.getCommand() == StompCommand.CONNECT) {
            String authorization = headers.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")
                    || !jwt.isAccessToken(authorization.substring(7))) {
                throw new MessageDeliveryException("Valid access token required");
            }
            CustomUserDetails user = users.loadUserByUsername(jwt.getEmailFromToken(authorization.substring(7)));
            headers.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } else if (headers.getCommand() == StompCommand.SEND || headers.getCommand() == StompCommand.SUBSCRIBE) {
            if (!(headers.getUser() instanceof UsernamePasswordAuthenticationToken authentication)) {
                throw new MessageDeliveryException("Authentication required");
            }
            String destination = headers.getDestination();
            if (destination == null) {
                throw new MessageDeliveryException("Destination required");
            }
            if (headers.getCommand() == StompCommand.SEND) {
                if (!GAME_DESTINATION.matcher(destination).matches()) {
                    throw new MessageDeliveryException("Destination is not allowed");
                }
            } else if (!destination.equals("/user/queue/errors") && !destination.equals("/user/queue/game-state")) {
                var match = GAME_TOPIC.matcher(destination);
                if (!match.matches()) {
                    throw new MessageDeliveryException("Subscription is not allowed");
                }
                CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
                games.checkSubscription(Long.parseLong(match.group(1)), user.user().getId());
            }
        }
        return message;
    }
}
