/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author 802996013
 */
public class MainTest {
    
    @Test
    public void testMain() {
        Main.main(new String[] {"configThumbNailGenTest"+Utils.resolveOS().name().toUpperCase()+".json"});
    }
}
