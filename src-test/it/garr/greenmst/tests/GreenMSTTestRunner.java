package it.garr.greenmst.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class GreenMSTTestRunner {
	
   public static void main(String[] args) {
      Result result = JUnitCore.runClasses(GreenMSTTestSuite.class);
      
      for (Failure failure : result.getFailures()) {
         System.err.println(failure.toString());
      }
      
      System.out.println(result.wasSuccessful());
   }

}  	