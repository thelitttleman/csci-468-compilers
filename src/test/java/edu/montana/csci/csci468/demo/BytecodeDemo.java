package edu.montana.csci.csci468.demo;

public class BytecodeDemo {

    int add(int i) {
        return i + 13;
    }

    public static void main(String[] args) {
        BytecodeDemo bytecodeDemo = new BytecodeDemo();
        bytecodeDemo.add(2);
    }

}
