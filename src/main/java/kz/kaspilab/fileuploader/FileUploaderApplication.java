package kz.kaspilab.fileuploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileUploaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileUploaderApplication.class, args);
    }

}

/*
План по загрузчику файлов

1. Как я поняла задачу

2. Возникшие вопросы/решения
-Какую бд использовать
-Загружать файл локально/удаленный сервис (абстракция хранилища)

3. Сложности/решения
-FilePart читается только один раз / temp file for hash calc and upload to store
-Калькуляция хэша файла или пароля - тяжелая операция (CPU-heavy)/ выполняем в отдельном потоке Bounded Elastic

4. Ключевые моменты решения

5. Больше времени - что изменилось бы?
-Orphan files - фоновая очистка
-Повторные попытки в случае падения
-Имплементация для хранения в удаленном хранилище
-Отдельный сервис для управления регистрации и логина пользователей (auth-server)

6. Работа кода + Производительность

 */