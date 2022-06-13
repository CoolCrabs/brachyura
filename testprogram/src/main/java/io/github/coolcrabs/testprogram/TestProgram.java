package io.github.coolcrabs.testprogram;

public class TestProgram {
    public static void main(String[] args) {
        HelloWorldProducerSingleton.INSTANCE.sayHello2World();
    }
    
    enum HelloWorldProducerSingleton {
        INSTANCE;
        
        void sayHello2World() {
            System.out.println("Hello World!");
        }
    }
}
