# Keycloak Service

Keycloak Service — это микросервис, который работает с Keycloak для управления пользователями и аутентификацией через REST API.

## Сервис работает на порту
http://localhost:7071

## Основные эндпоинты
- Работа с пользователями: http://localhost:7071/api/user/{command}
- Работа с аутентификацией: http://localhost:7071/api/auth/{command}

## Документация Swagger
Полная документация API доступна по ссылке: http://localhost:7071/swagger-ui/index.html#/

## Пользователи (по умолчанию)
| Роль    | Логин    | Пароль          |
|:--------|:---------|:----------------|
| ADMIN   | almas06  | admin-password  |
| TEACHER | doni06   | teacher-password |
| USER    | aibar06  | user-password    |

## Запуск через Docker
Для запуска всех сервисов используйте команду:
-- docker-compose up -d --
