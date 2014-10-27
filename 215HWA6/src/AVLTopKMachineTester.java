import junit.framework.TestCase;
import java.util.*;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public  class AVLTopKMachineTester extends TestCase{
  private Random r = new Random(); // Default seed comes from system time.
  
  public void testEmptyAVLTree(){//checks to see if an empty is created
    
    ITopKMachine emptyTree = new AVLTopKMachine (0);
    IsBalanced testTree = new IsBalanced();
    emptyTree.insert(10.0, 10.0);
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    assertTrue("Node membership test failed", emptyTree.memebership(3.2) != true);
    testTree.checkHeight(emptyTree.toString());
  }
  
  public void testElementAVLTree(){//checks to see if an element was added to the tree
    
    ITopKMachine emptyTree = new AVLTopKMachine (10);
    IsBalanced testTree = new IsBalanced();
    emptyTree.insert(10.0, 10.0);
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    assertTrue("Node membership test failed", emptyTree.memebership(10.0) == true);
    testTree.checkHeight(emptyTree.toString());
  }
  
  
  public void testFillgetTopK(){//checks to see if all the nodes are added to the arraylist
    
    ITopKMachine emptyTree = new AVLTopKMachine(10);
    IsBalanced testTree = new IsBalanced();
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    assertTrue("getTopK arraylist did not fill properly", emptyTree.getTopK().size() == 7);
    testTree.checkHeight(emptyTree.toString());
  }
  
  public void testgetCurrentCutoff(){//test the current cut off value
    
    ITopKMachine emptyTree = new AVLTopKMachine (3);
    IsBalanced testTree = new IsBalanced();
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    value = 0.134;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    System.out.println(emptyTree.toString());
    assertTrue("The Current Off Value is not correct.", emptyTree.getCurrentCutoff() == 1.2 );
   //testTree.checkHeight(emptyTree.toString()); 
  }
  
  public void testToString(){// check the toString method
    
    ITopKMachine emptyTree = new AVLTopKMachine (4);
    IsBalanced testTree = new IsBalanced();
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    value = 0.134;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    testTree.checkHeight(emptyTree.toString());
  }
  
  public void testBalancedTree(){//see if the tree is balanced
    
    ITopKMachine emptyTree = new AVLTopKMachine (7);
    IsBalanced testTree = new IsBalanced();
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    emptyTree.insert( 0.1, 0.1);
    testTree.checkHeight(emptyTree.toString());
    assertTrue("The Current Off Value is not correct.", emptyTree.getCurrentCutoff() == 6.2 );
    
  }
  
  public void testRightRotation(){//do a right rotation on the tree
    
    ITopKMachine emptyTree = new AVLTopKMachine (7);
    IsBalanced testTree = new IsBalanced();
    double value = 1.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value++;
    }
    
    testTree.checkHeight(emptyTree.toString());
  }
  
  public void testLeftRotation(){// do a left rotation on the tree
    
    ITopKMachine emptyTree = new AVLTopKMachine (3);
    IsBalanced testTree = new IsBalanced();
    double value = 7.2;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      value--;
    }
    
    testTree.checkHeight(emptyTree.toString());
    assertTrue("The Current Off Value is not correct.", emptyTree.getCurrentCutoff() == 3.2 );
    
  }
  
  
  public void testLeftRightRotation(){//do a left right rotation on the tree
    
    ITopKMachine emptyTree = new AVLTopKMachine (10);
    IsBalanced testTree = new IsBalanced();
    double value = 7.192;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      testTree.checkHeight(emptyTree.toString());
      value--;
    }
    value = 1.1;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      testTree.checkHeight(emptyTree.toString());
      value++;
    }
    testTree.checkHeight(emptyTree.toString());
  }
  
  
  public void testRightLeftRotation(){//do a right left rotation on the tree
    
    ITopKMachine emptyTree = new AVLTopKMachine (10);
    IsBalanced testTree = new IsBalanced();
    double value = 1.1;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      testTree.checkHeight(emptyTree.toString());
      value++;
    }
    value = 7.192;
    for (int i = 0; i < 7; i++){
      emptyTree.insert( value, value);
      testTree.checkHeight(emptyTree.toString());
      value--;
    }
    
    testTree.checkHeight(emptyTree.toString());
  }
  
  public void testBigTree(){// create a big tree and do all the rotations
    
    ITopKMachine emptyTree = new AVLTopKMachine (1000);
    IsBalanced testTree = new IsBalanced();
    double value = 7.192;
    double nextValue = 2.3535;
    double lastValue = .374;
    for (int i = 0; i < 100; i++){
      emptyTree.insert( value, value);
      emptyTree.insert(nextValue, nextValue);
      value--;
      nextValue++;
    }
    value = 1.1;
    for (int i = 0; i < 100; i++){
      emptyTree.insert( value, value);
      value++;
      lastValue--;
    }
    
    testTree.checkHeight(emptyTree.toString());
  }

  
  public void testNegativeKValue(){//see if a negative k works
    
    ITopKMachine emptyTree = new AVLTopKMachine (-10);
    IsBalanced testTree = new IsBalanced();
    double value = 7.192;
    double nextValue = 2.3535;
    double lastValue = .374;
    for (int i = 0; i < 100; i++){
      emptyTree.insert( value, value);
      emptyTree.insert(nextValue, nextValue);
      value--;
      nextValue++;
    }
    value = 1.1;
    for (int i = 0; i < 100; i++){
      emptyTree.insert( value, value);
      value++;
      lastValue--;
    }
    
    testTree.checkHeight(emptyTree.toString());
  }

  
  public void testRandomBigTree(){// do a random tree with big numbers
    
    ITopKMachine emptyTree = new AVLTopKMachine (1000);
    IsBalanced testTree = new IsBalanced();
    double value = 7.192;
    double nextValue = 2.3535;
    double lastValue = .374;
    for (int i = 0; i < 100; i++){
      emptyTree.insert( r.nextDouble(), value);
      emptyTree.insert(nextValue, nextValue);
      value--;
      nextValue++;
    }
    value = 1.1;
    for (int i = 0; i < 100; i++){
      emptyTree.insert( r.nextDouble(), value);
      value++;
      lastValue--;
    }
    
    testTree.checkHeight(emptyTree.toString());
  }
  
   public void testRandomSmallTree(){// do a random small tree with rotations
    
    ITopKMachine emptyTree = new AVLTopKMachine (3);
    IsBalanced testTree = new IsBalanced();
    double value = 7.192;
    double nextValue = 2.3535;
    double lastValue = .374;
    for (int i = 0; i < 0; i++){
      emptyTree.insert( nextValue, value);
      value--;
      nextValue++;
    }
    value = 1.1;
    for (int i = 0; i < 10; i++){
      emptyTree.insert( r.nextDouble(), r.nextDouble());
      value++;
      lastValue--;
    }
    
    testTree.checkHeight(emptyTree.toString());
  }
}