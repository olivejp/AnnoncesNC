package com.orlanth23.annoncesnc;

import org.junit.Test;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

import static org.junit.Assert.assertEquals;

public class TestRxJavaSuite {
    @Test
    public void testRxJava() {
        Observable<String> myObservable = Observable.just("Hello");
        Observer<String> myObserver = new Observer<String>() {
            @Override
            public void onCompleted() {
                // Called when the observable has no more data to emit
            }

            @Override
            public void onError(Throwable e) {
                // Called when the observable encounters an error
            }

            @Override
            public void onNext(String s) {
                // Called each time the observable emits data
                System.out.println("MY OBSERVER" +  s);
            }
        };

        Subscription subscription = myObservable.subscribe(myObserver);
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRxJava2(){
        Observable<Integer> myArrayObservable
                = Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}); // Emits each item of the array, one at a time

        myArrayObservable.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer i) {
                System.out.println("My Action" +  String.valueOf(i));
            }
        });

        assertEquals(4, 2 + 2);
    }
}
