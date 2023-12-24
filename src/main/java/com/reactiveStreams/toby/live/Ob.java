package com.reactiveStreams.toby.live;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class Ob {

    /*
     * ReactiveX
     * FRP -> Functional Reactive Programing -> 이벤트 방식의 프로그래밍?
     *
     * Duality (쌍대성)
     * 디자인 패턴의 Observer Pattern
     * Reactive Streams - 표준 - Java 9 API
     *
     * */

    public static void main(String[] args) {

        /*
         * List 는 Iterable 의 하위 타입입니다.
         * Implementing this interface allows an object to be the target of the enhanced for statement (sometimes called the "for-each loop" statement)
         * 위의 말은 Iterable 을 구현하는 모든 오브젝트는 for-each 가 가능하다는 말입니다.
         * 컬렉션이 아니더라도 원소 하나씩 순회할 수 있다는 것입니다.
         * */

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        for (Integer i : list) {
            System.out.println(i);
        }

        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);
        for (Integer i : iter) {
            System.out.println(i);
        }

        /*
         * Iterable 은 구현해야 할 메서드가 1개 밖에 없기 때문에 람다로 가능합니다.
         * 하지만 Iterator 는 구현해야 할 메서드가 많기 때문에 람다로는 불가능 합니다.
         *
         * */

        Iterable<Integer> iter3 = () ->
                new Iterator<>() {

                    int i = 0;
                    final static int MAX = 10;

                    @Override
                    public boolean hasNext() {
                        return i < MAX;
                    }

                    @Override
                    public Integer next() {
                        return ++i;
                    }
                };

        for (Integer i : iter3) {
            System.out.println(i);
        }

        for (Iterator<Integer> it = iter3.iterator(); it.hasNext(); ) {
            System.out.println(it.next());
        }

        /*
         * Iterable <---> Observable (쌍대성 = 궁극적인 기능은 똑같은데 표현이 반대입니다.)
         * Pull           Push (데이터를 멀어주는 방식)
         * 문을 잡아 땡겨서 여는거임
         * 문을 밀어서 여는거임
         *
         * */

        /*
         * Source -> Event/Data -> Observer
         * Update() 메서드 -> notifyObservers(i)가 호출되면 현재 옵저버블에 등록되어 있는 옵저버에게 전달됨
         * Pull은 정보를 줘 -> return 으로 값을 줘야 합니다.
         * Push는 데이터를 밀어 넣는 것 -> 따라서 return 값이 없습니다. void
         * DATA method() <--> method(DATA)
         *
         * */

        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        System.out.println("================");

        IntObservable io = new IntObservable();
        io.addObserver(ob);
//        io.run();

        /*
         * ExecutorService es = Executors.newSingleThreadExecutor(); 를 먼저 했는데
         * MAIN EXIT가 먼저 끝났습니다.
         * 별개의 쓰레드에서 동작을 하는 코드를 쉽게 작성할 수 있습니다(Push). 반면에 이터레이터를 사용하면 너무 어려워 집니다(Pull).
         *
         * */

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);
        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();


        /*
         * 1. Complete ??? -> 기존의 옵저버 패턴의 문제점은 완료라는 것을 표현할 방법이 없습니다.
         * 예를 들어 DB에 데이터를 긁어와서 던지는데 기존 옵저버 패턴은 완료라는 개념이 없습니다.
         *
         * 2. Error ???
         * run() 메서드에서 Exception이 발생한다면??
         * Exception이 버그때문에 발생할 수 도 있습니다. 이런거는 복구 가능한데 쓰레드로 비동기적으로 했을 때
         * 예외가 전파되는 방식, 받은 예외는 어떻게 처리하고 어떻게 재시도를 해야할지 과거의 패턴에는 없음.
         * 그래서 deprecated 가 됐음.
         *
         *
         * */

    }

    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();
                notifyObservers(i); // push
            }
        }
    }

}
