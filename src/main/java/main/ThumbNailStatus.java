/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author 802996013
 */
public class ThumbNailStatus {
    private final String name;
    private ThumbNailState state;

    public ThumbNailStatus(String name, ThumbNailState state) {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    
    public ThumbNailState getState() {
        return state;
    }

    public void setState(ThumbNailState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "state=" + state + " name=" + name ;
    }
    
    
}
