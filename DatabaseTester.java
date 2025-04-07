import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Комплексный тестер для демонстрации работы классов TableManager и ObjectStore.
 * Позволяет интерактивно тестировать различные функции этих классов.
 */
public class DatabaseTester {
    private static TableManager personManager;
    private static ObjectStore<Product> productStore;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("Демонстрация работы с базой данных Derby");
        System.out.println("============================================");
        
        scanner = new Scanner(System.in);
        
        try {
            // Инициализация TableManager для таблицы с людьми
            personManager = new TableManager("PERSONS");
            System.out.println("TableManager инициализирован для таблицы: " + personManager.getTableName());
            
            // Инициализация ObjectStore для таблицы с продуктами
            productStore = new ObjectStore<>("PRODUCTS");
            System.out.println("ObjectStore инициализирован для таблицы: " + productStore.getTableName());
            
            // Очистка таблиц для чистого теста
            personManager.clear();
            productStore.clear();
            System.out.println("Таблицы очищены");
            
            // Добавляем примеры данных
            populateSampleData();
            
            // Показываем главное меню
            showMainMenu();
        } finally {
            // Закрываем все соединения при завершении
            if (personManager != null) {
                personManager.close();
            }
            
            if (productStore != null) {
                productStore.close();
            }
            
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    
    /**
     * Заполняет таблицы примерами данных
     */
    private static void populateSampleData() {
        System.out.println("\nДобавление примеров данных...");
        
        // Добавляем людей через TableManager
        Person[] persons = {
            new Person("Иван", "Петров", 35),
            new Person("Мария", "Сидорова", 28),
            new Person("Алексей", "Иванов", 42),
            new Person("Елена", "Смирнова", 31),
        };
        
        for (Person person : persons) {
            personManager.add(person);
            System.out.println("+ Добавлен: " + person);
        }
        
        // Добавляем продукты через ObjectStore
        Product[] products = {
            new Product("Смартфон", 999.99, new Date()),
            new Product("Ноутбук", 1499.50, new Date()),
            new Product("Наушники", 199.99, new Date()),
            new Product("Планшет", 599.00, new Date()),
        };
        
        for (Product product : products) {
            productStore.add(product);
            System.out.println("+ Добавлен: " + product);
        }
        
        System.out.println("\nДанные добавлены успешно:");
        System.out.println("- Таблица " + personManager.getTableName() + ": " + personManager.size() + " записей");
        System.out.println("- Таблица " + productStore.getTableName() + ": " + productStore.size() + " записей");
    }
    
    /**
     * Отображает главное меню программы
     */
    private static void showMainMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n============= ГЛАВНОЕ МЕНЮ =============");
            System.out.println("1. Работа с TableManager (люди)");
            System.out.println("2. Работа с ObjectStore (продукты)");
            System.out.println("3. Сравнительная демонстрация");
            System.out.println("0. Выход");
            System.out.print("\nВыберите опцию: ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    workWithTableManager();
                    break;
                case "2":
                    workWithObjectStore();
                    break;
                case "3":
                    compareDemonstration();
                    break;
                case "0":
                    running = false;
                    System.out.println("Программа завершает работу...");
                    break;
                default:
                    System.out.println("Неизвестная опция. Пожалуйста, выберите корректный вариант.");
            }
        }
    }
    
    /**
     * Меню для работы с TableManager (люди)
     */
    private static void workWithTableManager() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n============= МЕНЮ TableManager =============");
            System.out.println("1. Просмотреть всех людей");
            System.out.println("2. Просмотреть человека по индексу");
            System.out.println("3. Добавить человека");
            System.out.println("4. Изменить человека");
            System.out.println("5. Удалить человека");
            System.out.println("6. Просмотреть диапазон людей");
            System.out.println("0. Вернуться в главное меню");
            System.out.print("\nВыберите опцию: ");
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        showAllPersons();
                        break;
                    case "2":
                        showPersonByIndex();
                        break;
                    case "3":
                        addPerson();
                        break;
                    case "4":
                        updatePerson();
                        break;
                    case "5":
                        removePerson();
                        break;
                    case "6":
                        showPersonRange();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Неизвестная опция. Пожалуйста, выберите корректный вариант.");
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Меню для работы с ObjectStore (продукты)
     */
    private static void workWithObjectStore() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n============= МЕНЮ ObjectStore =============");
            System.out.println("1. Просмотреть все продукты");
            System.out.println("2. Просмотреть продукт по индексу");
            System.out.println("3. Добавить продукт");
            System.out.println("4. Изменить продукт");
            System.out.println("5. Удалить продукт");
            System.out.println("6. Просмотреть диапазон продуктов");
            System.out.println("7. Изменить продукт напрямую и сохранить изменения");
            System.out.println("0. Вернуться в главное меню");
            System.out.print("\nВыберите опцию: ");
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        showAllProducts();
                        break;
                    case "2":
                        showProductByIndex();
                        break;
                    case "3":
                        addProduct();
                        break;
                    case "4":
                        updateProduct();
                        break;
                    case "5":
                        removeProduct();
                        break;
                    case "6":
                        showProductRange();
                        break;
                    case "7":
                        modifyAndSaveProduct();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Неизвестная опция. Пожалуйста, выберите корректный вариант.");
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Сравнительная демонстрация TableManager и ObjectStore
     */
    private static void compareDemonstration() {
        System.out.println("\n===== СРАВНИТЕЛЬНАЯ ДЕМОНСТРАЦИЯ =====");
        System.out.println("\n1. Сравнение производительности");
        
        // Демонстрация производительности
        System.out.println("\nДобавление 10 человек через TableManager...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            Person person = new Person("Test", "Person" + i, 20 + i);
            personManager.add(person);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
        
        System.out.println("\nДобавление 10 продуктов через ObjectStore...");
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            Product product = new Product("TestProduct" + i, 10.0 * i, new Date());
            productStore.add(product);
        }
        
        endTime = System.currentTimeMillis();
        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
        
        System.out.println("\n2. Сравнение функциональности");
        System.out.println("TableManager:");
        System.out.println("- Работает с объектами напрямую через базу данных");
        System.out.println("- Не реализует интерфейс коллекции");
        System.out.println("- Хорошо подходит для низкоуровневой работы с базой данных");
        
        System.out.println("\nObjectStore:");
        System.out.println("- Реализует интерфейс List<E>");
        System.out.println("- Поддерживает обобщенные типы");
        System.out.println("- Использует кэширование для ускорения операций");
        System.out.println("- Имеет методы для синхронизации кэша с базой данных");
        
        System.out.println("\nОба класса обеспечивают следующие преимущества:");
        System.out.println("- Автоматическая сериализация/десериализация объектов");
        System.out.println("- Постоянное хранение объектов в базе данных");
        System.out.println("- Работа с диапазонами объектов");
        System.out.println("- Простой интерфейс для CRUD-операций");
        
        System.out.println("\nНажмите Enter для продолжения...");
        scanner.nextLine();
    }
    
    // ========== Методы для работы с TableManager (люди) ==========
    
    private static void showAllPersons() {
        List<Object> persons = personManager.getAll();
        int size = persons.size();
        
        System.out.println("\nСписок всех людей (" + size + "):");
        if (size == 0) {
            System.out.println("Список пуст");
            return;
        }
        
        for (int i = 0; i < size; i++) {
            Person person = (Person) persons.get(i);
            System.out.println(i + ". " + person);
        }
    }
    
    private static void showPersonByIndex() {
        System.out.print("\nВведите индекс для просмотра (0-" + (personManager.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            Object obj = personManager.get(index);
            
            if (obj != null) {
                Person person = (Person) obj;
                System.out.println("Человек с индексом " + index + ": " + person);
            } else {
                System.out.println("Человек с индексом " + index + " не найден");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    private static void addPerson() {
        System.out.println("\nДобавление нового человека:");
        
        System.out.print("Имя: ");
        String firstName = scanner.nextLine();
        
        System.out.print("Фамилия: ");
        String lastName = scanner.nextLine();
        
        System.out.print("Возраст: ");
        int age = 0;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Некорректный возраст, будет использовано значение 0");
        }
        
        Person person = new Person(firstName, lastName, age);
        boolean success = personManager.add(person);
        
        if (success) {
            System.out.println("Человек успешно добавлен: " + person);
        } else {
            System.out.println("Не удалось добавить человека");
        }
    }
    
    private static void updatePerson() {
        showAllPersons();
        
        if (personManager.size() == 0) {
            return;
        }
        
        System.out.print("\nВведите индекс для обновления (0-" + (personManager.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            Object obj = personManager.get(index);
            
            if (obj != null) {
                Person oldPerson = (Person) obj;
                System.out.println("Обновление человека: " + oldPerson);
                
                System.out.print("Новое имя (или Enter для сохранения '" + oldPerson.getFirstName() + "'): ");
                String firstName = scanner.nextLine();
                if (firstName.isEmpty()) {
                    firstName = oldPerson.getFirstName();
                }
                
                System.out.print("Новая фамилия (или Enter для сохранения '" + oldPerson.getLastName() + "'): ");
                String lastName = scanner.nextLine();
                if (lastName.isEmpty()) {
                    lastName = oldPerson.getLastName();
                }
                
                System.out.print("Новый возраст (или Enter для сохранения '" + oldPerson.getAge() + "'): ");
                String ageStr = scanner.nextLine();
                int age = oldPerson.getAge();
                if (!ageStr.isEmpty()) {
                    try {
                        age = Integer.parseInt(ageStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Некорректный возраст, будет использовано предыдущее значение");
                    }
                }
                
                Person newPerson = new Person(firstName, lastName, age);
                Object result = personManager.set(index, newPerson);
                
                if (result != null) {
                    System.out.println("Человек успешно обновлен.");
                    System.out.println("Старое значение: " + oldPerson);
                    System.out.println("Новое значение: " + newPerson);
                } else {
                    System.out.println("Не удалось обновить человека");
                }
            } else {
                System.out.println("Человек с индексом " + index + " не найден");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    private static void removePerson() {
        showAllPersons();
        
        if (personManager.size() == 0) {
            return;
        }
        
        System.out.print("\nВведите индекс для удаления (0-" + (personManager.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            Object result = personManager.remove(index);
            
            if (result != null) {
                Person person = (Person) result;
                System.out.println("Человек успешно удален: " + person);
            } else {
                System.out.println("Не удалось удалить человека с индексом " + index);
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    private static void showPersonRange() {
        int size = personManager.size();
        
        if (size == 0) {
            System.out.println("Список людей пуст");
            return;
        }
        
        System.out.println("Общее количество людей: " + size);
        System.out.print("Введите начальный индекс (0-" + (size - 1) + "): ");
        
        try {
            int startIndex = Integer.parseInt(scanner.nextLine());
            if (startIndex < 0 || startIndex >= size) {
                System.out.println("Некорректный начальный индекс");
                return;
            }
            
            System.out.print("Введите конечный индекс (" + startIndex + "-" + (size - 1) + "): ");
            int endIndex = Integer.parseInt(scanner.nextLine());
            if (endIndex < startIndex || endIndex >= size) {
                System.out.println("Некорректный конечный индекс");
                return;
            }
            
            List<Object> persons = personManager.getRange(startIndex, endIndex);
            System.out.println("\nЛюди в диапазоне от " + startIndex + " до " + endIndex + ":");
            
            for (int i = 0; i < persons.size(); i++) {
                Person person = (Person) persons.get(i);
                System.out.println((startIndex + i) + ". " + person);
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректные числовые индексы");
        }
    }
    
    // ========== Методы для работы с ObjectStore (продукты) ==========
    
    private static void showAllProducts() {
        int size = productStore.size();
        
        System.out.println("\nСписок всех продуктов (" + size + "):");
        if (size == 0) {
            System.out.println("Список пуст");
            return;
        }
        
        for (int i = 0; i < size; i++) {
            Product product = productStore.get(i);
            System.out.println(i + ". " + product);
        }
    }
    
    private static void showProductByIndex() {
        System.out.print("\nВведите индекс для просмотра (0-" + (productStore.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            try {
                Product product = productStore.get(index);
                System.out.println("Продукт с индексом " + index + ": " + product);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Продукт с индексом " + index + " не найден");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    private static void addProduct() {
        System.out.println("\nДобавление нового продукта:");
        
        System.out.print("Название: ");
        String name = scanner.nextLine();
        
        System.out.print("Цена: ");
        double price = 0.0;
        try {
            price = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Некорректная цена, будет использовано значение 0.0");
        }
        
        Product product = new Product(name, price, new Date());
        boolean success = productStore.add(product);
        
        if (success) {
            System.out.println("Продукт успешно добавлен: " + product);
        } else {
            System.out.println("Не удалось добавить продукт");
        }
    }
    
    private static void updateProduct() {
        showAllProducts();
        
        if (productStore.size() == 0) {
            return;
        }
        
        System.out.print("\nВведите индекс для обновления (0-" + (productStore.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            try {
                Product oldProduct = productStore.get(index);
                System.out.println("Обновление продукта: " + oldProduct);
                
                System.out.print("Новое название (или Enter для сохранения '" + oldProduct.getName() + "'): ");
                String name = scanner.nextLine();
                if (name.isEmpty()) {
                    name = oldProduct.getName();
                }
                
                System.out.print("Новая цена (или Enter для сохранения '" + oldProduct.getPrice() + "'): ");
                String priceStr = scanner.nextLine();
                double price = oldProduct.getPrice();
                if (!priceStr.isEmpty()) {
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Некорректная цена, будет использовано предыдущее значение");
                    }
                }
                
                Product newProduct = new Product(name, price, new Date());
                Product updated = productStore.set(index, newProduct);
                
                System.out.println("Продукт успешно обновлен.");
                System.out.println("Старое значение: " + updated);
                System.out.println("Новое значение: " + newProduct);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Продукт с индексом " + index + " не найден");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    private static void removeProduct() {
        showAllProducts();
        
        if (productStore.size() == 0) {
            return;
        }
        
        System.out.print("\nВведите индекс для удаления (0-" + (productStore.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            try {
                Product removedProduct = productStore.remove(index);
                System.out.println("Продукт успешно удален: " + removedProduct);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Продукт с индексом " + index + " не найден");
            } catch (IllegalStateException e) {
                System.out.println("Ошибка при удалении: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    private static void showProductRange() {
        int size = productStore.size();
        
        if (size == 0) {
            System.out.println("Список продуктов пуст");
            return;
        }
        
        System.out.println("Общее количество продуктов: " + size);
        System.out.print("Введите начальный индекс (0-" + (size - 1) + "): ");
        
        try {
            int startIndex = Integer.parseInt(scanner.nextLine());
            if (startIndex < 0 || startIndex >= size) {
                System.out.println("Некорректный начальный индекс");
                return;
            }
            
            System.out.print("Введите конечный индекс (" + startIndex + "-" + (size - 1) + "): ");
            int endIndex = Integer.parseInt(scanner.nextLine());
            if (endIndex < startIndex || endIndex >= size) {
                System.out.println("Некорректный конечный индекс");
                return;
            }
            
            List<Product> products = productStore.subList(startIndex, endIndex + 1);
            System.out.println("\nПродукты в диапазоне от " + startIndex + " до " + endIndex + ":");
            
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                System.out.println((startIndex + i) + ". " + product);
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректные числовые индексы");
        }
    }
    
    private static void modifyAndSaveProduct() {
        showAllProducts();
        
        if (productStore.size() == 0) {
            return;
        }
        
        System.out.print("\nВведите индекс продукта для модификации (0-" + (productStore.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine());
            try {
                Product product = productStore.get(index);
                System.out.println("Текущий продукт: " + product);
                
                System.out.println("\nИзменяем продукт напрямую без вызова set()...");
                product.setName(product.getName() + " (модифицирован)");
                product.setPrice(product.getPrice() * 1.1); // Увеличиваем цену на 10%
                
                System.out.println("Продукт после модификации: " + product);
                System.out.println("Вызываем saveChanges() для сохранения изменений в базе данных...");
                
                productStore.saveChanges();
                System.out.println("Изменения сохранены.");
                
                System.out.println("\nПроверка, что изменения сохранены:");
                productStore.refresh(); // Перезагружаем данные из базы
                Product refreshedProduct = productStore.get(index);
                System.out.println("Продукт после перезагрузки: " + refreshedProduct);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Продукт с индексом " + index + " не найден");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректный числовой индекс");
        }
    }
    
    // ========== Классы моделей данных ==========
    
    /**
     * Класс, представляющий человека
     */
    public static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        private String firstName;
        private String lastName;
        private int age;
        
        public Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public int getAge() {
            return age;
        }
        
        @Override
        public String toString() {
            return firstName + " " + lastName + " (" + age + " лет)";
        }
    }
    
    /**
     * Класс, представляющий продукт
     */
    public static class Product implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private double price;
        private Date created;
        
        public Product(String name, double price, Date created) {
            this.name = name;
            this.price = price;
            this.created = created;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
        }
        
        public Date getCreated() {
            return created;
        }
        
        @Override
        public String toString() {
            return name + " ($" + String.format("%.2f", price) + ")";
        }
    }
}
