package assortment_of_things.misc;

import java.net.URL;
import java.net.URLClassLoader;

public class ReflectionClassLoader extends URLClassLoader
{
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ReflectionClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
