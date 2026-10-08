```markdown
## 2. Спецификация REST API

### 2.1. Аутентификация (`/api/auth`)

#### `POST /api/auth/register`
Регистрация нового пользователя.

* **Заголовки:** `Content-Type: application/json`
* **Тело запроса:**
```json
{
  "username": "grandmaster",
  "email": "player@example.com",
  "password": "SecurePassword123"
}

```

* **Ответ (201 Created):**

```json
{
  "id": 1,
  "username": "grandmaster",
  "email": "player@example.com",
  "createdAt": "2026-10-08T16:00:00Z"
}

```

* **Возможные ошибки:**
* `400 Bad Request` — невалидные данные (некорректный email, слишком короткий пароль).
* `409 Conflict` — пользователь с таким `username` или `email` уже зарегистрирован.



---

#### `POST /api/auth/login`

Вход в систему и получение JWT.

* **Заголовки:** `Content-Type: application/json`
* **Тело запроса:**

```json
{
  "username": "grandmaster",
  "password": "SecurePassword123"
}

```

* **Ответ (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxN...",
  "type": "Bearer",
  "userId": 1,
  "username": "grandmaster"
}

```

* **Возможные ошибки:**
* `401 Unauthorized` — неверный логин или пароль.



---

### 2.2. Управление партиями (`/api/games`)

#### `POST /api/games`

Создать новую партию. Создатель автоматически становится игроком за белых.

* **Заголовки:** `Authorization: Bearer <token>`
* **Тело запроса:** пустой объект `{}`
* **Ответ (201 Created):**

```json
{
  "id": 42,
  "whitePlayer": {
    "id": 1,
    "username": "grandmaster"
  },
  "blackPlayer": null,
  "status": "WAITING",
  "result": null,
  "resultReason": null,
  "currentFen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  "createdAt": "2026-10-08T16:05:00Z"
}

```

* **Возможные ошибки:**
* `401 Unauthorized` — отсутствует или невалиден токен авторизации.



---

#### `POST /api/games/{id}/join`

Присоединиться к существующей партии в качестве второго игрока (чёрные).

* **Заголовки:** `Authorization: Bearer <token>`
* **Параметры пути:** `id` — идентификатор партии.
* **Ответ (200 OK):**

```json
{
  "id": 42,
  "whitePlayer": {
    "id": 1,
    "username": "grandmaster"
  },
  "blackPlayer": {
    "id": 2,
    "username": "challenger"
  },
  "status": "IN_PROGRESS",
  "result": null,
  "resultReason": null,
  "currentFen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  "createdAt": "2026-10-08T16:05:00Z"
}

```

* **Возможные ошибки:**
* `400 Bad Request` — нельзя присоединиться к собственной партии или партия уже не находится в статусе `WAITING`.
* `404 Not Found` — партия с указанным `id` не найдена.



---

#### `GET /api/games/{id}`

Получить текущее состояние партии и всю историю ходов.

* **Заголовки:** `Authorization: Bearer <token>`
* **Параметры пути:** `id` — идентификатор партии.
* **Ответ (200 OK):**

```json
{
  "id": 42,
  "whitePlayer": {
    "id": 1,
    "username": "grandmaster"
  },
  "blackPlayer": {
    "id": 2,
    "username": "challenger"
  },
  "status": "IN_PROGRESS",
  "result": null,
  "resultReason": null,
  "currentFen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "moves": [
    {
      "ply": 1,
      "uci": "e2e4",
      "san": "e4",
      "fenAfter": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
      "createdAt": "2026-10-08T16:06:00Z"
    }
  ],
  "createdAt": "2026-10-08T16:05:00Z",
  "finishedAt": null
}

```

* **Возможные ошибки:**
* `404 Not Found` — партия с таким `id` не существует.



---

#### `GET /api/games/my`

Получить историю партий текущего авторизованного пользователя.

* **Заголовки:** `Authorization: Bearer <token>`
* **Ответ (200 OK):**

```json
[
  {
    "id": 42,
    "whitePlayer": {
      "id": 1,
      "username": "grandmaster"
    },
    "blackPlayer": {
      "id": 2,
      "username": "challenger"
    },
    "status": "FINISHED",
    "result": "WHITE_WIN",
    "resultReason": "CHECKMATE",
    "createdAt": "2026-10-08T16:05:00Z",
    "finishedAt": "2026-10-08T16:20:00Z"
  }
]

```

---

#### `POST /api/games/{id}/resign`

Сдаться в текущей партии.

* **Заголовки:** `Authorization: Bearer <token>`
* **Параметры пути:** `id` — идентификатор партии.
* **Ответ (200 OK):**

```json
{
  "id": 42,
  "status": "FINISHED",
  "result": "BLACK_WIN",
  "resultReason": "RESIGN",
  "finishedAt": "2026-10-08T16:15:00Z"
}

```

* **Возможные ошибки:**
* `400 Bad Request` — игрок не является участником этой партии или партия уже завершена.



---

## 3. Спецификация WebSocket (STOMP)

* **Точка подключения:** `ws://localhost:8080/ws`
* **Авторизация:** Передаётся заголовок STOMP CONNECT:
```text
CONNECT
Authorization:Bearer <JWT_TOKEN>

```



### 3.1. Отправка хода на сервер (Client -> Server)

* **Destination:** `/app/games/{id}/move`
* **Payload:**

```json
{
  "from": "e2",
  "to": "e4",
  "promotion": null
}

```

*(При превращении пешки поле `promotion` содержит букву фигуры: `"q"`, `"r"`, `"b"`, `"n"`)*.

---

### 3.2. Подписка и события сервера (Server -> Client)

* **Subscription Topic:** `/topic/games/{id}`

#### Событие: `MOVE` (Сделан ход)

```json
{
  "type": "MOVE",
  "gameId": 42,
  "lastMove": {
    "ply": 1,
    "uci": "e2e4",
    "san": "e4"
  },
  "currentFen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "turn": "BLACK"
}

```

#### Событие: `PLAYER_JOINED` (Второй игрок присоединился)

```json
{
  "type": "PLAYER_JOINED",
  "gameId": 42,
  "blackPlayer": {
    "id": 2,
    "username": "challenger"
  },
  "status": "IN_PROGRESS"
}

```

#### Событие: `GAME_OVER` (Партия завершена)

```json
{
  "type": "GAME_OVER",
  "gameId": 42,
  "status": "FINISHED",
  "result": "WHITE_WIN",
  "resultReason": "CHECKMATE",
  "winnerUsername": "grandmaster",
  "finalFen": "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4"
}

```

---

## 4. Проектирование экранов приложения (UI Wireframes)

### 1. Авторизация и регистрация (`/login`, `/register`)

```text
+--------------------------------------------------+
|                   Онлайн Шахматы                 |
|                                                  |
|   Имя пользователя: [                         ]  |
|   Пароль:           [                         ]  |
|                                                  |
|   [     Войти     ]    или   [ Регистрация ]     |
+--------------------------------------------------+

```

### 2. Лобби (`/lobby`)

```text
+------------------------------------------------------------------+
| Онлайн Шахматы  | Лобби | Профиль (grandmaster)     | [Выйти]    |
+------------------------------------------------------------------+
|  [ + Создать новую партию ]                                      |
|                                                                  |
|  Ожидающие соперников партии:                                    |
|  ----------------------------------------------------------------+
|  ID #42 | Белые: grandmaster | 2 минуты назад     | [ Играть ]   |
|  ID #41 | Белые: alex_chess   | 5 минут назад     | [ Играть ]   |
+------------------------------------------------------------------+

```

### 3. Игровой экран (`/game/:id`)

```text
+------------------------------------------------------------------+
| Партия #42: grandmaster (Белые) vs challenger (Чёрные)           |
+---------------------------------+--------------------------------+
|                                 | Ход: Белые                     |
|                                 | Статус: ИДЁТ ИГРА              |
|        [ ШАХМАТНАЯ ДОСКА ]      | -------------------------------+
|       (react-chessboard)        | История ходов:                 |
|                                 | 1. e4 e5                       |
|                                 | 2. Nf3 Nc6                     |
|                                 | 3. Bc4 ...                     |
|                                 | -------------------------------+
|                                 | [ Сдаться ]                    |
+---------------------------------+--------------------------------+

```

### 4. Профиль и история партий (`/profile`)

```text
+---------------------------------------------------------------------+
| Профиль игрока: grandmaster                                         |
| Сыграно игр: 10 | Побед: 7 | Поражений: 2 | Ничьих: 1               |
+---------------------------------------------------------------------+
| История партий:                                                     |
| #42 | Против challenger | Белые | Победа (Мат)    | 08.10.2026      |
| #35 | Против pro_player  | Чёрные | Поражение (Сдался)| 07.10.2026  |
+---------------------------------------------------------------------+

```


