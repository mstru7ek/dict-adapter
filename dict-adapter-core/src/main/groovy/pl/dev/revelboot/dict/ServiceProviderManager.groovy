package pl.dev.revelboot.dict

import pl.dev.revelboot.dict.provider.ContentProviderService

import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.WatchKey
import java.util.concurrent.CountDownLatch

import static groovy.io.FileType.FILES
import static java.nio.file.StandardWatchEventKinds.*

class ServiceProviderManager {

    final Object lock = new Object()


    static {
        def serviceLib = System.getenv("SERVICE_LIB")
        if (serviceLib == null || serviceLib.isEmpty() ){
            throw new IllegalStateException("!!! env variable 'SERVICE_LIB' not exists ")
        }
    }

    static libPath = Paths.get(System.getenv("SERVICE_LIB"))

    ServiceLoader<ContentProviderService> serviceLoader

    def init() {

        if (!libPath.toFile().exists()) {
            throw new IllegalStateException("!!! lib path those NOT EXISTS: ${libPath.toAbsolutePath()}")
        }

        def watchService = FileSystems.getDefault().newWatchService()

        libPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

        def isInitialized = new CountDownLatch(1)
        def watcher = new Thread({

            refreshContextClassLoader()
            synchronized (lock) {
                serviceLoader = ServiceLoader.load(ContentProviderService.class)
            }
            isInitialized.countDown()

            WatchKey key
            while ((key = watchService.take()) != null) {

                // single reload for any modification in lib path
                key.pollEvents()

                println("Reload ...")

                refreshContextClassLoader()
                synchronized (lock) {
                    serviceLoader = ServiceLoader.load(ContentProviderService.class)
                }
                key.reset()
            }
        })

        watcher.start()

        isInitialized.await()
    }

    private refreshContextClassLoader() {

        List<URL> jars = []
        libPath.toFile().eachFileRecurse(FILES) {
            if (it.name.endsWith('.jar')) {
                jars.add(it.toURI().toURL())
            }
        }
        URLClassLoader libClassLoader = new URLClassLoader(jars.toArray() as URL[], Thread.currentThread().getContextClassLoader())

        Thread.currentThread().contextClassLoader = libClassLoader
    }

    List<ContentProviderService> getProviders() {
        synchronized (lock) {
            return serviceLoader.collect()
        }
    }

}
