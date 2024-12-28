package io.github.seehiong.utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DisposableUtil {

    private static final List<Disposable> disposables = new ArrayList<>();

    public void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    public void disposeSubscriptions() {
        log.info("disposing all subscriptions");
        for (var disposable : disposables) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        disposables.clear();
    }

}
