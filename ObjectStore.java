import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


public class ObjectStore<E extends Serializable> extends AbstractList<E> implements List<E> {
    
    private final TableManager tableManager;
    private final List<E> cache;
    

    public ObjectStore(String tableName) {
        this.tableManager = new TableManager(tableName);
        this.cache = loadCache();
    }
    

    @Override
    public int size() {
        return cache.size();
    }
    
  
    @Override
    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + size());
        }
        return cache.get(index);
    }
    
  
    @Override
    public boolean add(E element) {
        boolean result = tableManager.add(element);
        if (result) {
            cache.add(element);
        }
        return result;
    }
    
  
    @Override
    public E remove(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + size());
        }
        
        E removed = cache.get(index);
        Object result = tableManager.remove(index);
        
        if (result != null) {
            cache.remove(index);
            return removed;
        }
        
        throw new IllegalStateException("Не удалось удалить элемент из базы данных");
    }
    
    
    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + size());
        }
        
        E oldValue = cache.get(index);
        Object result = tableManager.set(index, element);
        
        if (result != null) {
            cache.set(index, element);
            return oldValue;
        }
        
        throw new IllegalStateException("Не удалось обновить элемент в базе данных");
    }
    
  
    @Override
    public void clear() {
        tableManager.clear();
        cache.clear();
    }
  
    public void close() {
        tableManager.close();
    }
    
  
    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }
    
   
    public boolean dispose() {
        boolean result = tableManager.dispose();
        if (result) {
            cache.clear();
        }
        return result;
    }
    
    
    public void saveChanges() {
        tableManager.clear();
        for (E element : cache) {
            tableManager.add(element);
        }
    }
  
    public void refresh() {
        cache.clear();
        cache.addAll(loadCache());
    }
    
   
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size: " + size());
        }
        return cache.subList(fromIndex, toIndex);
    }
    
    
    public TableManager getTableManager() {
        return tableManager;
    }
    
  
    public String getTableName() {
        return tableManager.getTableName();
    }
    
   
    @SuppressWarnings("unchecked")
	private List<E> loadCache() {
        List<Object> objects = tableManager.getAll();
        List<E> result = new ArrayList<>(objects.size());
        
        for (Object obj : objects) {
            if (obj != null) {
                result.add((E) obj);
            }
        }
        
        return result;
    }
}