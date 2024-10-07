import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

class TestRobot{
  public static void main(String[] args){

    Robot robot = new Robot();
        // Move the mouse to position (500, 500)
        robot.mouseMove(1000, 1000);

        // Simulate a left mouse click
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        // Simulate typing the letter 'A'
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
    
  }

  
}
