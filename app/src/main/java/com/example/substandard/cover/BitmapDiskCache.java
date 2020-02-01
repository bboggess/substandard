package com.example.substandard.cover;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * I learned about this from the example linked at
 * https://developer.android.com/topic/performance/graphics/cache-bitmap
 */
public class BitmapDiskCache {
    private static final String JOURNAL = "journal";
    private static final String READ = "READ";
    private static final String CREATE = "CREATE";
    private static final String REMOVE = "REMOVE";

    private static final int IMAGE_QUALITY = 100;
    private static final Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.PNG;
    private static final String FILE_EXTENSION = ".png";


    /*
     * Keep track of cache using a journal file. Lines will look like
     *
     * READ [id]
     * CREATE [id] [fileSize]
     * REMOVE [id]
     *
     * The read event tracks usage, the create event signifies a new file
     * has been added, and remove indicated removal from the cache. The
     * journal is appended to whenever one of these events is performed on
     * the cache.
     */
    // this is cache in memory. Just keeps file IDs and file size, NOT actual
    // files, in memory. The LinkedHashMap can be made to order based on access automatically!
    private final LinkedHashMap<String, Entry> entries;
    private final long maxSize;
    private long size;
    private final File journalFile;
    private FileWriter journalWriter;

    /**
     * Used to clear up cache space on a separate thread when we get past
     * the max space. It makes zero sense to make the user wait for space
     * to be cleared before having the new file inserted.
     */
    private final ExecutorService cacheCleanService = Executors.newSingleThreadExecutor();
    private final Callable<Void> cacheCleanCallable = () -> {
        synchronized (BitmapDiskCache.this) {
            clearCacheSpace();
        }

        return null;
    };


    private BitmapDiskCache(long maxSize, File cacheDirectory) {
        this.maxSize = maxSize;
        // .75f is default, but you can't just pass in initial capacity and accessOrder...
        entries = new LinkedHashMap<>(0, .75f, true);
        journalFile = new File(cacheDirectory, JOURNAL);
    }

    public static BitmapDiskCache getCache(File cacheDirectory, long maxSize) throws IOException {
        BitmapDiskCache cache = new BitmapDiskCache(maxSize, cacheDirectory);
        if (cache.journalFile.exists()) {
            cache.readJournal();
            cache.processJournal();
            cache.journalWriter = new FileWriter(cache.journalFile);
        } else {
            cache.journalFile.createNewFile();
        }

        return cache;
    }

    /**
     * Literally just reconstructs the entries map from the journal file, line by line.
     */
    private void readJournal() {
        try (BufferedReader reader = new BufferedReader(new FileReader(journalFile))) {
            String currentLine;
            while (null != (currentLine = reader.readLine())) {
                readJournalLine(currentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJournalLine(String line) throws IOException {
        final String errorMessage = "unexpected journal line: " + line;

        String[] words = line.split(" ");
        if (words.length < 2) {
            throw new IOException(errorMessage);
        }
        String key = words[1];
        Entry entry = entries.get(key);
        if (null == entry) {
            entry = new Entry(key);
            entries.put(key, entry);
        }

        if (line.startsWith(CREATE) && words.length == 3) {
            long size = Long.parseLong(words[2]);
            entry.setSize(size);
        } else if (line.startsWith(READ) && words.length == 2) {
            // line was accessed and needs to be moved to back of queue
            // BUT LinkedHashMap already takes care of this
        } else if (line.startsWith(REMOVE) && words.length == 2) {
            entries.remove(key);
        } else {
            throw new IOException(errorMessage);
        }
    }

    /**
     * All setup needed after loading entries into memory. Right now, this is just
     * updating the size member variable.
     */
    private void processJournal() {
        for (Entry entry : entries.values()) {
            size += entry.getSize();
        }
    }

    public synchronized void put(String key, Bitmap bitmap) throws IOException {
        String filename = getImageFilename(key);
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            bitmap.compress(IMAGE_FORMAT, IMAGE_QUALITY, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        long bitmapSize = bitmap.getByteCount();

        Entry entry = new Entry(key);
        entries.put(key, entry);
        size += bitmapSize;
        writeToJournal(entry, CREATE);

        cacheCleanService.submit(cacheCleanCallable);
    }

    private String getImageFilename(String key) {
        return key + FILE_EXTENSION;
    }

    // It is ridiculous to me that this isn't a LinkedHashMap method...
    private synchronized String getOldestKey() {
        String oldestKey = null;
        for (String key : entries.keySet()) {
            oldestKey = key;
        }

        return oldestKey;
    }

    private void removeOldest() throws IOException {
        removeEntry(getOldestKey());
    }

    private synchronized void removeEntry(String key) throws IOException {
        Entry toRemove = entries.get(key);
        entries.remove(key);
        size -= toRemove.getSize();
        writeToJournal(toRemove, REMOVE);
    }

    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    public synchronized Bitmap get(String key) throws IOException {
        Entry entry = entries.get(key);
        writeToJournal(entry, READ);
        return loadImageFromDisk(entry);
    }

    private Bitmap loadImageFromDisk(Entry entry) {
        if (null == entry) {
            return null;
        }

        String filename = getImageFilename(entry.getId());
        return BitmapFactory.decodeFile(filename);
    }

    private void clearCacheSpace() throws IOException {
        while (size > maxSize) {
            removeOldest();
        }
    }

    private synchronized  void writeToJournal(Entry entry, String action) throws IOException {
        if (!CREATE.equals(action) || !READ.equals(action) || !REMOVE.equals(action)){
            throw new IllegalArgumentException("Not a valid journal entry: " +  action);
        }

        if (null == journalWriter) {
            journalWriter = new FileWriter(journalFile);
        }

        String toWrite = action + " " + entry.getId();
        if (CREATE.equals(toWrite)) {
            toWrite += " " + entry.getSize();
        }

        journalWriter.write(toWrite);
    }

    /**
     * Represents an entry in the cache.
     */
    class Entry {
        private String id;
        private long size;

        public Entry(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (null == obj || null == id || !(obj instanceof Entry)) {
                return false;
            }

            Entry that = (Entry) obj;
            return id.equals(that.getId());
        }
    }
}
