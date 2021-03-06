import java.lang.NullPointerException;

import javax.swing.text.html.HTMLDocument.Iterator;

class SparseDoubleVector extends ADoubleVector {

  private int vectorLength;
  private double offset;
  private ISparseArray<Double> dict = new SparseArray<Double>();


  public SparseDoubleVector(int firstArg, double secondArg) {// 23456782345678
    vectorLength = firstArg;
    offset = secondArg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#getItem(int)
   */
  public double getItem(int theIndex) throws OutOfBoundsException {
    // Make sure theIndex is in bounds
    if (theIndex <= -1) {
      throw new OutOfBoundsException("Index does not exist.");
    }
    if (theIndex >= vectorLength) {
      throw new OutOfBoundsException("Index does not exist.");
    }
    // Return values that correspond to the index
    if (dict.get(theIndex) == null) {
      return offset;
    } else {
      return dict.get(theIndex) + offset;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#addMyselfToHim(IDoubleVector)
   */
  public void addMyselfToHim(IDoubleVector addThisOne) throws OutOfBoundsException {
    // Vector must be the same length
    if (addThisOne.getLength() != vectorLength) {
      throw new OutOfBoundsException("Lengths do not match up.");
    }
    // Go through all the indices to perform the full arithmetic
    int thePosition = 0;
    while (thePosition < vectorLength) {
      addThisOne.setItem(thePosition, addThisOne.getItem(thePosition) + this.getItem(thePosition));
      thePosition++;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#addToAll(double)
   */
  public void addToAll(double addMe) {// adds a value to all the values in the list
    offset = offset + addMe;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#getRoundedItem(int)
   */
  public long getRoundedItem(int whichOne) {
    // returns value rounded up to an integer
    double value = dict.get(whichOne) + offset;
    long rounded = (long) Math.round(value);
    return rounded;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#normalize()
   */
  public void normalize() {
    // divides all the elements by the length of the list
    double i = l1Norm();
    for (int j = 0; j < vectorLength; j++) {
      if (dict.get(j) == null) {
        continue;
      }
      dict.put(j, (dict.get(j)) / i);
    }
    offset /= i;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#getLength()
   */
  public int getLength() {
    // gets the length of the list
    return vectorLength;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#l1Norm()
   */
  public double l1Norm() {
    // the sum of all entries
    double sum = 0;
    for (IIndexedData<Double> i : dict) {
      sum += i.getData();
    }
    return sum + (vectorLength) * offset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    // vector into a string
    String str = " ";
    for (int i = 0; i < vectorLength; i++) {
      try {
        str += getItem(i) + " ";
      } catch (OutOfBoundsException e) {
        e.printStackTrace();
      }
    }
    return str;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ADoubleVector#setItem(int, double)
   */
  public void setItem(int whichOne, double setToMe) throws OutOfBoundsException {
    // Test to see if in bounds
    if (whichOne >= vectorLength) {
      throw new OutOfBoundsException("Index does not exist.");
    }
    if (whichOne <= -1) {
      throw new OutOfBoundsException("Index does not exist.");
    }
    // set the specified item
    dict.put(whichOne, setToMe - offset);
  }

}
