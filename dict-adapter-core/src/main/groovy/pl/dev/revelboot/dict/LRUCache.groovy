package pl.dev.revelboot.dict

import pl.dev.revelboot.dict.provider.ContentProviderResponse

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

class LRUCache implements Cache {

    private final ReentrantLock lock = new ReentrantLock()

    private final Map<String, List<ContentProviderResponse>> map = new ConcurrentHashMap<>()
    private final Deque<String> queue = new LinkedList<String>()
    private final int limit

    LRUCache(int limit) {
        this.limit = limit
    }

    @Override
    List<ContentProviderResponse> get(String key) {
        def object = map.get(key)
        if(object != null){
            removeThenAddKey(key)
        }
        return object
    }

    @Override
    void put(String key, List<ContentProviderResponse> value) {
        def oldValue = map.put(key, value)
        if (oldValue != null) {
            removeThenAddKey(key)
        } else {
            addKey(key)
        }
        if (map.size() > limit) {
            map.remove(removeLast())
        }

    }

    private void removeThenAddKey(String key) {
        lock.lock()
        try {
            queue.removeFirstOccurrence(key)
            queue.addFirst(key)
        } finally {
            lock.unlock()
        }

    }

    private String removeLast() {
        lock.lock()
        try {
            final String removedKey = queue.removeLast()
            return removedKey
        } finally {
            lock.unlock()
        }
    }


    private void addKey(String key) {
        lock.lock()
        try {
            queue.addFirst(key)
        } finally {
            lock.unlock()
        }
    }

}
