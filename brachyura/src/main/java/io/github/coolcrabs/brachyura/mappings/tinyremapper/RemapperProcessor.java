package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.coolcrabs.brachyura.mappings.tinyremapper.RemapperProcessor.BruhFileSystemProvider.BruhFileSystem.BruhPath;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper.JarType;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import net.fabricmc.tinyremapper.InputTag;
import net.fabricmc.tinyremapper.TinyRemapper;

//TODO update when tr finally doesn't require paths for sources
public class RemapperProcessor implements Processor {
    final TinyRemapper remapper;
    final List<Path> classpath;

    public RemapperProcessor(TrWrapper tr, List<Path> classpath) {
        this.remapper = tr.tr;
        this.classpath = classpath;
    }

    @Override
    public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
        BruhFileSystemProvider bruh = new BruhFileSystemProvider();
        for (Path j : classpath) {
            TinyRemapperHelper.readJar(remapper, j, JarType.CLASSPATH);
        }
        HashMap<ProcessingSource, InputTag> tags = new HashMap<>();
        for (ProcessingEntry e : inputs) {
            tags.computeIfAbsent(e.id.source, k -> remapper.createInputTag());
        }
        for (ProcessingEntry e : inputs) {
            if (e.id.path.endsWith(".class")) {
                remapper.readInputs(tags.get(e.id.source), bruh.child.createPath(e));
            } else {
                sink.sink(e.in, e.id);
            }
        }
        for (Map.Entry<ProcessingSource, InputTag> entry : tags.entrySet()) {
            remapper.apply((path, bytes) -> sink.sink(() -> new ByteArrayInputStream(bytes), new ProcessingId(path + ".class", entry.getKey())), entry.getValue());
        }
    }

    class BruhFileSystemProvider extends FileSystemProvider {
        BruhFileSystem child = new BruhFileSystem();

        class BruhFileSystem extends FileSystem {

            @Override
            public FileSystemProvider provider() {
                return BruhFileSystemProvider.this;
            }
    
            @Override
            public void close() throws IOException {
                // stup
            }
    
            @Override
            public boolean isOpen() {
                //  Auto-generated method stub
                return false;
            }
    
            @Override
            public boolean isReadOnly() {
                //  Auto-generated method stub
                return false;
            }
    
            @Override
            public String getSeparator() {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public Iterable<Path> getRootDirectories() {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public Iterable<FileStore> getFileStores() {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public Set<String> supportedFileAttributeViews() {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public Path getPath(String first, String... more) {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public PathMatcher getPathMatcher(String syntaxAndPattern) {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public UserPrincipalLookupService getUserPrincipalLookupService() {
                //  Auto-generated method stub
                return null;
            }
    
            @Override
            public WatchService newWatchService() throws IOException {
                //  Auto-generated method stub
                return null;
            }

            BruhPath createPath(ProcessingEntry e) {
                BruhPath result = new BruhPath();
                result.entry = e;
                return result;
            }
    
            class BruhPath implements Path {
                ProcessingEntry entry;
    
                @Override
                public FileSystem getFileSystem() {
                    return BruhFileSystem.this;
                }
    
                @Override
                public boolean isAbsolute() {
                    //  Auto-generated method stub
                    return false;
                }
    
                @Override
                public Path getRoot() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path getFileName() {
                    return this;
                }
    
                @Override
                public Path getParent() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public int getNameCount() {
                    //  Auto-generated method stub
                    return 0;
                }
    
                @Override
                public Path getName(int index) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path subpath(int beginIndex, int endIndex) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public boolean startsWith(Path other) {
                    //  Auto-generated method stub
                    return false;
                }
    
                @Override
                public boolean startsWith(String other) {
                    //  Auto-generated method stub
                    return false;
                }
    
                @Override
                public boolean endsWith(Path other) {
                    //  Auto-generated method stub
                    return false;
                }
    
                @Override
                public boolean endsWith(String other) {
                    //  Auto-generated method stub
                    return false;
                }
    
                @Override
                public Path normalize() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path resolve(Path other) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path resolve(String other) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path resolveSibling(Path other) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path resolveSibling(String other) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path relativize(Path other) {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public URI toUri() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path toAbsolutePath() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Path toRealPath(LinkOption... options) throws IOException {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public File toFile() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public Iterator<Path> iterator() {
                    //  Auto-generated method stub
                    return null;
                }
    
                @Override
                public int compareTo(Path other) {
                    //  Auto-generated method stub
                    return 0;
                }

                @Override
                public String toString() {
                    return entry.id.path;
                }
                
            }
        }

        @Override
        public String getScheme() {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public FileSystem getFileSystem(URI uri) {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public Path getPath(URI uri) {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
                FileAttribute<?>... attrs) throws IOException {
            try (InputStream in = ((BruhPath)path).entry.in.get()) {
                return new SeekableInMemoryByteChannel(StreamUtil.readFullyAsBytes(in));
            }
        }

        @Override
        public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
            //  Auto-generated method stub
            
        }

        @Override
        public void delete(Path path) throws IOException {
            //  Auto-generated method stub
            
        }

        @Override
        public void copy(Path source, Path target, CopyOption... options) throws IOException {
            //  Auto-generated method stub
            
        }

        @Override
        public void move(Path source, Path target, CopyOption... options) throws IOException {
            //  Auto-generated method stub
            
        }

        @Override
        public boolean isSameFile(Path path, Path path2) throws IOException {
            //  Auto-generated method stub
            return false;
        }

        @Override
        public boolean isHidden(Path path) throws IOException {
            //  Auto-generated method stub
            return false;
        }

        @Override
        public FileStore getFileStore(Path path) throws IOException {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public void checkAccess(Path path, AccessMode... modes) throws IOException {
            //  Auto-generated method stub
            
        }

        @Override
        public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
                throws IOException {
            return (A) new BasicFileAttributes() {

                @Override
                public FileTime lastModifiedTime() {
                    //  Auto-generated method stub
                    return null;
                }

                @Override
                public FileTime lastAccessTime() {
                    //  Auto-generated method stub
                    return null;
                }

                @Override
                public FileTime creationTime() {
                    //  Auto-generated method stub
                    return null;
                }

                @Override
                public boolean isRegularFile() {
                    //  Auto-generated method stub
                    return true;
                }

                @Override
                public boolean isDirectory() {
                    //  Auto-generated method stub
                    return false;
                }

                @Override
                public boolean isSymbolicLink() {
                    //  Auto-generated method stub
                    return false;
                }

                @Override
                public boolean isOther() {
                    //  Auto-generated method stub
                    return false;
                }

                @Override
                public long size() {
                    //  Auto-generated method stub
                    return 0;
                }

                @Override
                public Object fileKey() {
                    //  Auto-generated method stub
                    return null;
                }

            };
        }

        @Override
        public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
                throws IOException {
            //  Auto-generated method stub
            return null;
        }

        @Override
        public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
            //  Auto-generated method stub
            
        }

        @Override
        public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
            return ((BruhPath)path).entry.in.get();
        }
    }

    // Stolen from apache
    // WHY TR WHYYYY
    static class SeekableInMemoryByteChannel implements SeekableByteChannel {

        private static final int NAIVE_RESIZE_LIMIT = Integer.MAX_VALUE >> 1;
    
        private byte[] data;
        private final AtomicBoolean closed = new AtomicBoolean();
        private int position, size;
    
        /**
         * Constructor taking a byte array.
         *
         * <p>This constructor is intended to be used with pre-allocated buffer or when
         * reading from a given byte array.</p>
         *
         * @param data input data or pre-allocated array.
         */
        public SeekableInMemoryByteChannel(final byte[] data) {
            this.data = data;
            size = data.length;
        }
    
        /**
         * Constructor taking a size of storage to be allocated.
         *
         * <p>Creates a channel and allocates internal storage of a given size.</p>
         *
         * @param size size of internal buffer to allocate, in bytes.
         */
        public SeekableInMemoryByteChannel(final int size) {
            this(new byte[size]);
        }
    
        /**
         * Returns this channel's position.
         *
         * <p>This method violates the contract of {@link SeekableByteChannel#position()} as it will not throw any exception
         * when invoked on a closed channel. Instead it will return the position the channel had when close has been
         * called.</p>
         */
        @Override
        public long position() {
            return position;
        }
    
        @Override
        public SeekableByteChannel position(final long newPosition) throws IOException {
            ensureOpen();
            if (newPosition < 0L || newPosition > Integer.MAX_VALUE) {
                throw new IOException("Position has to be in range 0.. " + Integer.MAX_VALUE);
            }
            position = (int) newPosition;
            return this;
        }
    
        /**
         * Returns the current size of entity to which this channel is connected.
         *
         * <p>This method violates the contract of {@link SeekableByteChannel#size} as it will not throw any exception when
         * invoked on a closed channel. Instead it will return the size the channel had when close has been called.</p>
         */
        @Override
        public long size() {
            return size;
        }
    
        /**
         * Truncates the entity, to which this channel is connected, to the given size.
         *
         * <p>This method violates the contract of {@link SeekableByteChannel#truncate} as it will not throw any exception when
         * invoked on a closed channel.</p>
         *
         * @throws IllegalArgumentException if size is negative or bigger than the maximum of a Java integer
         */
        @Override
        public SeekableByteChannel truncate(final long newSize) {
            if (newSize < 0L || newSize > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Size has to be in range 0.. " + Integer.MAX_VALUE);
            }
            if (size > newSize) {
                size = (int) newSize;
            }
            if (position > newSize) {
                position = (int) newSize;
            }
            return this;
        }
    
        @Override
        public int read(final ByteBuffer buf) throws IOException {
            ensureOpen();
            int wanted = buf.remaining();
            final int possible = size - position;
            if (possible <= 0) {
                return -1;
            }
            if (wanted > possible) {
                wanted = possible;
            }
            buf.put(data, position, wanted);
            position += wanted;
            return wanted;
        }
    
        @Override
        public void close() {
            closed.set(true);
        }
    
        @Override
        public boolean isOpen() {
            return !closed.get();
        }
    
        @Override
        public int write(final ByteBuffer b) throws IOException {
            ensureOpen();
            int wanted = b.remaining();
            final int possibleWithoutResize = size - position;
            if (wanted > possibleWithoutResize) {
                final int newSize = position + wanted;
                if (newSize < 0) { // overflow
                    resize(Integer.MAX_VALUE);
                    wanted = Integer.MAX_VALUE - position;
                } else {
                    resize(newSize);
                }
            }
            b.get(data, position, wanted);
            position += wanted;
            if (size < position) {
                size = position;
            }
            return wanted;
        }
    
        /**
         * Obtains the array backing this channel.
         *
         * <p>NOTE:
         * The returned buffer is not aligned with containing data, use
         * {@link #size()} to obtain the size of data stored in the buffer.</p>
         *
         * @return internal byte array.
         */
        public byte[] array() {
            return data;
        }
    
        private void resize(final int newLength) {
            int len = data.length;
            if (len <= 0) {
                len = 1;
            }
            if (newLength < NAIVE_RESIZE_LIMIT) {
                while (len < newLength) {
                    len <<= 1;
                }
            } else { // avoid overflow
                len = newLength;
            }
            data = Arrays.copyOf(data, len);
        }
    
        private void ensureOpen() throws ClosedChannelException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
        }
    
    }
}
