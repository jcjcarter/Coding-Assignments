import java.util.*;

class TreeSparseArray<T> implements ISparseArray<T> {

  Map<Integer, T> treeMap = new TreeMap<Integer, T>();


  /**
   * Add element to the array at position.
   * 
   * @param position position in the array
   * @param element data to place in the array
   */
  public void put(int position, T element) {
    treeMap.put(position, element);
  }

  /**
   * Get element at the given position.
   *
   * @param position position in the array
   * @return element at that position or null if there is none
   */
  public T get(int position) {
    return treeMap.get(position);
  }

  /**
   * Create an iterator over the array.
   *
   * @return an iterator over the sparse array
   */
  public Iterator<IIndexedData<T>> iterator() {
    Iterator<IIndexedData<T>> myIter = new TreeIndexedIterator<T>(treeMap);
    return myIter;
  }

  /**
   * Empty constructor.
   */
  public TreeSparseArray() {}

  class TreeIndexedIterator<T> implements Iterator<IIndexedData<T>> {

    private Map<Integer, T> mapIn;
    private Iterator<Integer> keys;


    /**
     * Constructor that takes in a tree map.
     * 
     * @param treeMap
     */
    public TreeIndexedIterator(Map<Integer, T> treeMap) {
      mapIn = treeMap;
      keys = mapIn.keySet().iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return keys.hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public IIndexedData<T> next() {
      int index = keys.next();
      T data = mapIn.get(index);
      IndexedData<T> retVal = new IndexedData<T>(index, data);
      return retVal;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new RuntimeException("There should not be any removes called.");
    }

  }
}
