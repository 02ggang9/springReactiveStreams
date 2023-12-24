package com.reactiveStreams.toby.live;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Iterator;

public class PubSub {

    public static void main(String[] args) {
        /*
         * Publisher <- Observable
         * Subscriber <- Observer
         *
         * subscription
         * 기존은 onSubscribe() 만 호출하면 됐는데 이제는 subscription 을 넣어서 줘야 합니다.
         * 푸쉬 방식인데 왠 요청?
         * 백 프레셔(역압) publisher 와 subscriber 의 속도차이가 발생하게 되는데 이를 subscription 을 통해서
         * 조절하도록 합니다.
         *
         * 만약, Publisher 는 너무 빠르고 Subscribe 는 하나를 처리하는데 느리다면 어디서 버퍼를 만들거나
         * 정보가 유실될 수 있습니다. 그 반대도 마찬가지이기 때문에 Reqeust 를 통해서 스케일링을 합니다.
         *
         * 이렇게 스케일링은 한다면 여러가지 장점이 있습니다. 메모리 사용량이 피크를 치지말고 캐쉬에 저장했다가
         * 천천히 끌어가면 항상 일정한 크기를 유지하도록 할 수 있습니다. (넷플릭스에서 적용함)
         * */

        Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);

        Publisher<Integer> p = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> s) {

                Iterator<Integer> it = itr.iterator();

                s.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        while (n-- > 0) {
                            if (it.hasNext()) {
                                s.onNext(it.next());
                            } else {
                                s.onComplete();
                                break;
                            }
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {

            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                this.subscription = s;
                this.subscription.request(3);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println("onNext" + item);
                // 3개 받고 조금 쉬었다가 3개 더 받을꺼야라는 코드는 여기서 작성
                System.out.println("breakTime");
                this.subscription.request(1);

            }

            @Override
            public void onError(Throwable t) {
                // Subscriber 는 try-catch 구문이 필요 없음.
                System.out.println("onError");

            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        p.subscribe(s);

    }

}
