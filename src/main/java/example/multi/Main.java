package example.multi;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {
       // example1();
        example2();
        example3();
    }

    private static void example3() {
        Lock lock = new ReentrantLock();
        Thread tA= new Thread(()->{
            System.out.println("поток А стартует");
            lock.lock();
            try {
                System.out.println("поток А вошел в критическую секцию");
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(50);
                    System.out.println("поток А работает");
                }
                System.out.println("поток А выходит из критической секции");
            }catch (InterruptedException e) {   System.out.println("ой-ой");    }
            finally { lock.unlock();  }

            try {    Thread.sleep(50);       } catch (InterruptedException e) {  throw new RuntimeException(e);   }
            System.out.println("поток А заканчивается");
        });
        Thread tB= new Thread(()->{
            System.out.println("поток B стартует");
            lock.lock();
            try {
                System.out.println("поток B вошел в критическую секцию");
                for (int i = 0; i < 4; i++) {
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("поток B работает");
                }
                System.out.println("поток B выходит из критической секции");
            }
            finally {  lock.unlock();}
            try {    Thread.sleep(60);       } catch (InterruptedException e) {  throw new RuntimeException(e);   }
            System.out.println("поток B заканчивается");
        });
        tA.start();tB.start();

        Thread tC= new Thread(()->{
            System.out.println("поток C стартует");

            if(  lock.tryLock()){
                try{
                    System.out.println("поток C вошел в критическую секцию");
                    for (int i = 0; i <5; i++) {
                        Thread.sleep(30);
                    }
                    System.out.println("поток C работает");
                }
                catch (InterruptedException e){ System.out.println("ой"); }
                finally {
                    System.out.println("поток C выходит из критической секции");
                    lock.unlock();
                }
            }
            else{
                System.out.println("поток C плюнул и ушел");
            }

            try {    Thread.sleep(60);       } catch (InterruptedException e) {  throw new RuntimeException(e);   }
            System.out.println("поток C заканчивается");
        });
        tC.start();
        Thread tD= new Thread(()->{
            System.out.println("поток D стартует");
            try{
                if(  lock.tryLock(600, TimeUnit.MILLISECONDS)){
                    System.out.println("поток D вошел в критическую секцию");
                    for (int i = 0; i <5; i++) {
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) { System.out.println("ой"); }
                        System.out.println("поток D работает");
                    }
                    System.out.println("поток D выходит из критической секции");
                    lock.unlock();
                }
                else System.out.println("поток D плюнул и ушел");
                Thread.sleep(60);
            }
            catch (InterruptedException e) {  throw new RuntimeException(e);   }
            System.out.println("поток D заканчивается");
        });
        tD.start();
    }

    private static void example2() throws InterruptedException {
        AtomicReferenceFieldUpdater<Person, String> renamer = AtomicReferenceFieldUpdater.newUpdater(Person.class, String.class, "name");
        Person p = new Person("Вася");
        Thread t3 = new Thread(()-> {
            renamer.compareAndSet(p, "Вася", "Пупкин");
           // p.setName( "Пупкин");
            System.out.println("переименовали");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("p внутри потока = " + p);
            System.out.println("end");
        });
        t3.start();
        System.out.println("p = " + p);
        t3.join();
        System.out.println("p = " + p);
    }

    private static void example1() {
        AtomicInteger a= new AtomicInteger(1);

        Thread t1 = new Thread(()->{
            System.out.println("начало 1");
            for (int i = 0; i < 7; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {    throw new RuntimeException(e);}
                System.out.println(a);
                a.getAndSet(a.get() + 1);
            }
            System.out.println("конец 1");
        });
        t1.start();
        Thread t2 = new Thread(()->{
            System.out.println("начало 2");
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {    throw new RuntimeException(e);}
                System.out.println(a);
                a.getAndSet(a.get()*2);
            }
            System.out.println("конец 2");
        });
        t2.start();
    }
}

class Person{
   public volatile String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                '}';
    }
}