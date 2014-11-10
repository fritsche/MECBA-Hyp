/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.main;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author giovani
 */
public class Teste {

    public static void main(String[] args) {
        ArrayList<Integer> parent1 = new ArrayList<>();
        ArrayList<Integer> parent2 = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            parent1.add(i);
            parent2.add(i);
        }

        Collections.shuffle(parent1);
        Collections.shuffle(parent2);

        for (Integer value : parent1) {
            System.out.print(value + ", ");
        }
        System.out.println("");
        for (Integer value : parent2) {
            System.out.print(value + ", ");
        }
        System.out.println("");
    }

}
