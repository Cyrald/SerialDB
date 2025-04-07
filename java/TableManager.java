import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TableManager {
    private String tableName;
    private Database db;
    
    public TableManager(String tableName) {
        this.tableName = tableName.toUpperCase();
        this.db = new Database();
        
        if (!db.isTableExist(this.tableName)) {
            db.createTable(this.tableName);
        }
    }
    
    public int size() {
        return db.getTableSize(tableName);
    }
    
    public Object get(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        
        List<byte[]> data = db.readDataInRange(tableName, index, index);
        if (data.isEmpty()) {
            return null;
        }
        
        return byteToObject(data.get(0));
    }
    
  
    public boolean add(Object obj) {
        return db.writeData(tableName, objectToByte(obj));
    }
    
    public Object remove(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        
        Object oldValue = get(index);
        boolean success = db.deleteDataByIndex(tableName, index);
        
        return success ? oldValue : null;
    }
    
   
    public Object set(int index, Object element) {
        if (index < 0 || index >= size()) {
            return null;
        }
        
        Object oldValue = get(index);
        boolean success = db.overwriteDataByIndex(tableName, index, objectToByte(element));
        
        return success ? oldValue : null;
    }
    
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
 
    public void clear() {
        db.deleteTable(tableName);
        db.createTable(tableName);
    }
    
   
    public void close() {
        db.close();
    }
  
    public boolean dispose() {
        boolean result = db.deleteTable(tableName);
        return result;
    }
    
  
    public String getTableName() {
        return tableName;
    }
    
   
    public List<Object> getAll() {
        List<byte[]> byteData = db.readData(tableName);
        List<Object> result = new ArrayList<>();
        
        for (byte[] bytes : byteData) {
            Object obj = byteToObject(bytes);
            if (obj != null) {
                result.add(obj);
            }
        }
        
        return result;
    }
 
    public List<Object> getRange(int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex < startIndex || endIndex >= size()) {
            return new ArrayList<>();
        }
        
        List<byte[]> byteData = db.readDataInRange(tableName, startIndex, endIndex);
        List<Object> result = new ArrayList<>();
        
        for (byte[] bytes : byteData) {
            Object obj = byteToObject(bytes);
            if (obj != null) {
                result.add(obj);
            }
        }
        
        return result;
    }
    
  
    private Object byteToObject(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        
        Object obj = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            obj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }
    
   
    private byte[] objectToByte(Object obj) {
        if (obj == null) {
            return new byte[0];
        }
        
        byte[] bytes = new byte[0];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
    
   
    public Database getDatabase() {
        return db;
    }
}