public class Java8BruhMoment {
    public static void main(String[] args) {
        bruh(() -> {
            System.out.println("Bruh Moment");
        });
    }

    static void bruh(Runnable runnable) {
        runnable.run();
    }
}
