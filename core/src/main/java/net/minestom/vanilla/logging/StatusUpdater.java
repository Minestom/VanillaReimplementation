package net.minestom.vanilla.logging;

public interface StatusUpdater {
    /**
     * Updates the progress bar without changing the text message.
     * @param progress the progress, between 0 and 1
     */
    void progress(double progress);

    /**
     * Updates the text message without changing the progress bar.
     * @param message the new message
     */
    void message(String message);

    void update();
}
