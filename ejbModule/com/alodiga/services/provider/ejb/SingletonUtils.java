/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.alodiga.services.provider.ejb;

/**
 *
 * @author christiang
 */

class SingletonUtils {

       private static SingletonUtils theInstance;

    public static SingletonUtils getInstance() {
        if (theInstance == null) {
            theInstance = new SingletonUtils();
        }

        return theInstance;
    }

 

    private SingletonUtils() {
        super();
    }



}
