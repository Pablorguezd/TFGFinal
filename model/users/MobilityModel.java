/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.users;

/**
 *
 * @author paco
 */
abstract class MobilityModel {
    
    abstract public int[] move(int[] current, double tics);
    
}
