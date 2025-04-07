# DerbyStore: Java Object Persistence Library

Java library for storing objects in Derby database.

## 📦 Installation

1. **Minimal dependencies for ObjectStore**:

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.derby</groupId>
        <artifactId>derby</artifactId>
        <version>10.12.1.1</version>
    </dependency>
</dependencies>
```

2. **Dependencies for DerbyDatabaseViewer (GUI)**:

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.derby</groupId>
        <artifactId>derby</artifactId>
        <version>10.12.1.1</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.13.3</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-xml</artifactId>
        <version>2.13.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.12.0</version>
    </dependency>
</dependencies>
```

3. **Copy the classes from this repository to your project**

## 🔍 Usage Examples

### Storing different types of objects

```java
// Create object store
ObjectStore<Object> store = new ObjectStore<>("mixed_objects");

// Add different types of objects
store.add("Just a string");
store.add(42);
store.add(3.14159);
store.add(new Date());
store.add(new ArrayList<String>() {{ add("item1"); add("item2"); }});
store.add(new HashMap<String, Integer>() {{ put("key1", 100); put("key2", 200); }});

// Get objects
String str = (String) store.get(0);
Integer num = (Integer) store.get(1);
Double pi = (Double) store.get(2);
Date date = (Date) store.get(3);
ArrayList<String> list = (ArrayList<String>) store.get(4);
HashMap<String, Integer> map = (HashMap<String, Integer>) store.get(5);

// Don't forget to close the store
store.close();
```

## 📚 Main Classes and Methods

### ObjectStore

Main class for storing objects in the database. Implements the `List<E>` interface.

```java
// Create a store
ObjectStore<T> store = new ObjectStore<>("table_name");

// Main methods
store.add(object);          // Add object
store.get(index);           // Get object by index
store.set(index, object);   // Update object at index
store.remove(index);        // Delete object at index
store.size();               // Get number of objects in store
store.clear();              // Clear the store
store.close();              // Close database connection
store.refresh();            // Refresh cache from database
store.saveChanges();        // Save all changes (transaction)
store.dispose();            // Completely delete the table
```

### TableManager

Low-level class for managing database tables.

```java
TableManager manager = new TableManager("table_name");
manager.add(object);        // Add object
manager.get(index);         // Get object by index
manager.set(index, object); // Update object
manager.remove(index);      // Delete object
manager.getAll();           // Get all objects
manager.clear();            // Clear table
manager.close();            // Close connection
```

### DerbyDatabaseViewer

GUI for viewing and managing Derby databases.

```java
// Launch the viewer
DerbyDatabaseViewer viewer = new DerbyDatabaseViewer();
viewer.setVisible(true);
```

## 📄 License

MIT License
