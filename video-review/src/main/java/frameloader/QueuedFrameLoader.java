package frameloader;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Assumes that all frames are in the folder.
 */
public class QueuedFrameLoader extends FrameLoaderBase {
    // queue size constants
    private static final int PRE_QUEUE_CAPACITY = 130;
    private static final int POST_QUEUE_CAPACITY = 30;

    // queues that are holding the frames
    private final ArrayDeque<Frame> preQueue;
    private final ArrayDeque<Frame> postQueue;

    // wrapper classes for queues so that we are able to swap queues
    private final LoaderDeque loaderDeque;
    private final DumperDeque dumperDeque;

    // threads that are and removes frame from the queues
    private Thread loader;
    private Thread dumper;

    // flags
    private final AtomicBoolean isRunning;
    private final AtomicBoolean goesForward;
    private final AtomicBoolean eventOccurredLoader;
    private final AtomicBoolean eventOccurredDumper;
    private final AtomicBoolean loaderWaitingOutside;
    private final AtomicBoolean dumperWaitingOutside;

    // to control the frequency of frame grabbing
    private long lastFrameGrab;

    public QueuedFrameLoader(ObjectProperty<Image> imageProperty) {
        super(imageProperty);
        lastFrameGrab = System.currentTimeMillis();

        // create queues
        preQueue = new ArrayDeque<>(PRE_QUEUE_CAPACITY);
        postQueue = new ArrayDeque<>(POST_QUEUE_CAPACITY);

        // create queue wrapper objects for threads
        loaderDeque = new LoaderDeque();
        dumperDeque = new DumperDeque();

        // flags
        isRunning = new AtomicBoolean(true);
        goesForward = new AtomicBoolean(true);
        eventOccurredLoader = new AtomicBoolean(false);
        eventOccurredDumper = new AtomicBoolean(false);
        loaderWaitingOutside = new AtomicBoolean(false);
        dumperWaitingOutside = new AtomicBoolean(false);
    }

    /**
     * Checks if enough time has passed from the previous frame grab.
     *
     * @return <code>true</code> if not enough time passed, <code>false</code> otherwise
     */
    public boolean checkDelay() {
        // Linear Delay Growth: ms = -(30/0.7) * remaining_capacity_percent + 42.85714
        //int delay = (int) (-42.85 * (loaderDeque.getDeque().size() / (double) PRE_QUEUE_CAPACITY) + 42.85);
        // Polynomial Delay Growth: ms = 50 * (-(remaining_capacity_percent - 1))^degree
        int delay = (int) (50.0 * Math.pow(-((loaderDeque.getDeque().size() / (double) PRE_QUEUE_CAPACITY) - 1), 2.75));
        return (System.currentTimeMillis() - lastFrameGrab) < delay;
    }

    @Override
    public void loadFolder(Path framesFolder) {
        super.loadFolder(framesFolder);
        // set forward direction as default
        goesForward.compareAndSet(false, true);
        // preset the flags so the threads are waiting before they got into there routine
        eventOccurredLoader.set(true);
        eventOccurredDumper.set(true);
        // create and start threads if not already happened
        boolean wasCreated = createAndStartThread();
        // wait until threads are outside of routine
        resetThreadState(wasCreated);
        // clear both threads
        preQueue.clear();
        postQueue.clear();
    }

    @Override
    public void nextFrame() {
        // do not execute if at the end of array
        if (getCurrentFrame() != null && getIndex(getCurrentFrame().getFrameNumber()) >= getFramesArray().length - 1)
            return;
        if (checkDelay())
            return;

        // force threads to go outside of routine so that we can change settings,
        // if the current direction is not forward
        if (goesForward.compareAndSet(false, true)) {
            eventOccurredLoader.set(true);
            eventOccurredDumper.set(true);
            // wait until threads are outside of routine
            resetThreadState(false);
            // get index of last loaded frame
            int startIdx = peekStartIndex() + 1;
            // swap queues
            loaderDeque.setDeque(preQueue, startIdx);
            dumperDeque.setDeque(postQueue);
        }

        // wake up thread so they can go into the loading/dumping routine
        synchronized (loaderDeque) {
            loaderDeque.notify();
        }
        synchronized (dumperDeque) {
            dumperDeque.notify();
        }

        // because we don't have a capacity-restricted queue we can just place it on top without concerns
        dumperDeque.getDeque().offerFirst(getCurrentFrame());
        // loads next frame from queue
        loadNextFrameAsCurrentFrame();
        lastFrameGrab = System.currentTimeMillis();

        // set frame number from filename
//        setFrameInfo();
    }

    @Override
    public void previousFrame() {
        // do not execute if at the end of array
        if (getCurrentFrame() != null && getIndex(getCurrentFrame().getFrameNumber()) <= 0)
            return;

        // triggers if delay is not big enough
        if (checkDelay())
            return;

        if (goesForward.compareAndSet(true, false)) {
            eventOccurredLoader.set(true);
            eventOccurredDumper.set(true);
            // wait until threads are outside of routine
            resetThreadState(false);
            // get index of last loaded frame
            int startIdx = peekStartIndex() - 1;
            // swap queues
            loaderDeque.setDeque(postQueue, -startIdx);
            dumperDeque.setDeque(preQueue);
        }

        // wake up thread so they can go into the loading/dumping routine
        synchronized (loaderDeque) {
            loaderDeque.notify();
        }
        synchronized (dumperDeque) {
            dumperDeque.notify();
        }

        // because we don't have a capacity-restricted queue we can just place it on top without concerns
        dumperDeque.getDeque().offerFirst(getCurrentFrame());
        // loads next frame from queue
        loadNextFrameAsCurrentFrame();
        lastFrameGrab = System.currentTimeMillis();
        // display frame number from filename

        // set frame number from filename
//        setFrameInfo();
    }

    @Override
    public void jumpToFrame(int frameNumber) {
        if (frameNumber < 0)
            return;

        int frameIdx = getIndex(frameNumber);
        // do not execute if outside of array
        if (frameIdx < 0 || frameIdx >= getFramesArray().length)
            return;

        // set forward direction on any jump as default
        goesForward.compareAndSet(false, true);
        // preset the flags so the threads are waiting before they got into there routine
        eventOccurredLoader.set(true);
        eventOccurredDumper.set(true);
        // create and start threads if not already happened
        boolean wasCreated = createAndStartThread();
        // wait until threads are outside of routine
        resetThreadState(wasCreated);
        // clear both threads
        preQueue.clear();
        postQueue.clear();
        // swap queues
        loaderDeque.setDeque(preQueue, frameIdx);
        dumperDeque.setDeque(postQueue);

        // wake up thread to begin the loading/dumping routine
        synchronized (loaderDeque) {
            loaderDeque.notify();
        }
        synchronized (dumperDeque) {
            dumperDeque.notify();
        }

        // load frame from queue
        loadNextFrameAsCurrentFrame();
        // display frame number from filename
//        frameNumberProperty.set(extractFrameNumberFromUrl(currentFrame.getUrl()));
    }

    @Override
    public void skipFrames(int delta) {
        // TODO: Instead jumping, look if the frame number is within the queue (avoid reloading the whole queue)
        jumpToFrame(getCurrentFrame().getFrameNumber() + delta);
    }

    /**
     * Get a starting index from the end of dumping queue
     *
     * @return frame index of queue tail
     */
    private int peekStartIndex() {
        Frame peekedFrame = dumperDeque.getDeque().peekLast();
        if (peekedFrame != null)
            return getIndex(peekedFrame.getFrameNumber());
        else return getIndex(getCurrentFrame().getFrameNumber());
    }

    /**
     * Wait until threads are waiting outside of the loading/dumping routine
     *
     * @param wasCreated <code>true</code> if threads were created before, <code>false</code> otherwise.
     *                   Because if the threads were newly created then they will start the waiting outside
     *                   position because the flags are setting beforehand.
     */
    private void resetThreadState(final boolean wasCreated) {
        // put queues out of loading/dumping routine and wait outside
        synchronized (loaderDeque) {
            if (!wasCreated)
                loaderDeque.notify();
            while (!loaderWaitingOutside.compareAndSet(true, false)) {
                synchronized (loaderDeque) {
                    try {
                        loaderDeque.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        synchronized (dumperDeque) {
            if (!wasCreated)
                dumperDeque.notify();
            while (!dumperWaitingOutside.compareAndSet(true, false)) {
                synchronized (dumperDeque) {
                    try {
                        dumperDeque.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Loads the next frame from queue and saves it as current frame
     */
    private void loadNextFrameAsCurrentFrame() {
        // wait if queue doesn't have at least one frame inside
        synchronized (loaderDeque) {
            while (loaderDeque.getDeque().size() <= 0) {
                try {
                    loaderDeque.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // get frame from queue
        setFrame(loaderDeque.getDeque().pollFirst());
        // notify threads that it can continue loading if it's waiting
        synchronized (loaderDeque) {
            loaderDeque.notify();
        }
    }

    /**
     * Creates an starts the threads if it's not already created/started
     */
    private boolean createAndStartThread() {
        if (dumper == null && loader == null) {
            dumper = new Dumper();
            loader = new Loader();
            dumper.setName("Image Dumper");
            loader.setName("Image Loader");
            loader.setPriority(Thread.MAX_PRIORITY);
            dumper.setPriority(Thread.MAX_PRIORITY);
            dumper.start();
            loader.start();
            return true;
        }
        return false;
    }

    public void stopThreads() {
        // flag that threads need to stop
        isRunning.compareAndSet(true, false);
        // wake up any threat that could be sleeping
        // they will then notice the isRunning flag
        synchronized (loaderDeque) {
            loaderDeque.notifyAll();
        }
        synchronized (dumperDeque) {
            dumperDeque.notifyAll();
        }

        // interrupt the threads if all above methods fail
        if (loader != null)
            loader.interrupt();
        if (dumper != null)
            dumper.interrupt();
    }

    /**
     * Thread class dedicated to always fill up the queue
     */
    private class Loader extends Thread {
        private Frame loadFrame(int index) {
            if (index >= 0 && index < getFramesArray().length)
                return new Frame(getFramesFolder().resolve(getFramesArray()[index]));
            else return null;
        }

        private void loading() {
            int frameIdx = loaderDeque.getStartIndex();
            while (isRunning.get() && !eventOccurredLoader.get()) {
                Frame nextFrame = loadFrame(frameIdx);
                if (nextFrame != null) {
                    synchronized (loaderDeque) {
                        while (isRunning.get() && loaderDeque.getDeque().size() >= PRE_QUEUE_CAPACITY && !eventOccurredLoader.get()) {
                            try {
                                loaderDeque.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (isRunning.get() && !eventOccurredLoader.get()) {
                        loaderDeque.getDeque().offerLast(nextFrame);
                        synchronized (loaderDeque) {
                            loaderDeque.notify();
                        }
                        frameIdx += loaderDeque.sign();
                    }
                }
            }
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                while (eventOccurredLoader.compareAndSet(true, false)) {
                    loaderWaitingOutside.compareAndSet(false, true);
                    synchronized (loaderDeque) {
                        try {
                            loaderDeque.notify();
                            loaderDeque.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                loading();
            }
        }
    }

    /**
     * Thread class dedicated to remove frame from the queue if full
     */
    private class Dumper extends Thread {
        private void dumping() {
            while (isRunning.get() && !eventOccurredDumper.get()) {
                synchronized (dumperDeque) {
                    while (isRunning.get() && dumperDeque.getDeque().size() < POST_QUEUE_CAPACITY && !eventOccurredDumper.get()) {
                        try {
                            dumperDeque.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (isRunning.get() && !eventOccurredDumper.get()) {
                    dumperDeque.getDeque().pollLast();
                    synchronized (dumperDeque) {
                        dumperDeque.notify();
                    }
                }
            }
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                while (eventOccurredDumper.compareAndSet(true, false)) {
                    dumperWaitingOutside.compareAndSet(false, true);
                    synchronized (dumperDeque) {
                        try {
                            dumperDeque.notify();
                            dumperDeque.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                dumping();
            }
        }
    }

    /**
     * This class is an wrapper class for a queue so the queues are swappable.
     */
    private static class LoaderDeque {
        private Deque<Frame> deque;
        private int startIndex;
        private byte sign;

        public void setDeque(Deque<Frame> deque, int startIndex) {
            this.deque = deque;
            sign = (byte) (startIndex < 0 ? -1 : 1);
            this.startIndex = Math.abs(startIndex);
        }

        /**
         * Defines the direction of counting
         *
         * @return <code>1</code> for forward and <code>-1</code> for backward counting
         */
        public byte sign() {
            return sign;
        }

        public Deque<Frame> getDeque() {
            return deque;
        }

        public int getStartIndex() {
            return startIndex;
        }
    }

    /**
     * This class is an wrapper class for a queue so the queues are swappable.
     */
    private static class DumperDeque {
        private Deque<Frame> deque;

        public Deque<Frame> getDeque() {
            return deque;
        }

        public void setDeque(Deque<Frame> deque) {
            this.deque = deque;
        }
    }
}
