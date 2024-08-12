# Сетевой чат

## Описание проекта
Проект содержит два консольных приложения для сетевого чата, описываемых классами Client и Server в пакетах client и server.
Для демонстрации можно запустить файлы Main.java в каждом пакете.

### Клиент
Клиент — это двухпоточное приложение, которое умеет устанавливать соединение с сервером и взаимодействовать с ним по протоколу, описанному ниже. Клиент может задать своё имя, получать новые сообщения из чата, отправлять свои сообщения в чат или выйти, закрыв соединение.
- Один поток отвечает за ожидание клиентского ввода и отправку его на сервер
- Второй поток отвечает за чтение новых сообщений от сервера и вывод их на экран

### Сервер
Сервер — это многопоточное приложение, обрабатывающее взаимодействия с клиентами.
- Изначально выделяется пул потоков под клиентов
- Один поток в цикле ожидает новых клиентов
- При подключении нового клиента его обрабатывает отдельный поток, который отвечает за отправку сообщений клиенту
- Этот поток порождает дополнительный поток, отвечающий за чтение сообщений от клиента

### Описание протокола
- Клиент и сервер обмениваются текстовыми сообщениями через сокет
- При подключении клиента сервер запрашивает у него имя, которое будет передаваться другим участника чата вместе с каждым сообщением клиента
- Клиент получает историю чата в виде сообщений до текущего момента, а также получает все новые сообщения по мере поступления
- После отправки имени клиент может отправлять любые сообщения, которые будут доставлены всем остальным участникам чата

### Настройки
Настройки для клиента и сервера хранятся в /resources/client_settings.txt и в /resources/server_settings.txt

### Логирование
Логи всех событий для клиента и сервера записываются в файлы логов, указанные в настройках