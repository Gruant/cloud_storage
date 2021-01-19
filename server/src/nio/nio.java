package nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

class Nio{

    private final ServerSocketChannel serverChannel = ServerSocketChannel.open();
    private final Selector selector = Selector.open();
    private final ByteBuffer buffer = ByteBuffer.allocate(5);
    private Path serverPath = Paths.get("serverDir");

    public Nio() throws IOException {
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (serverChannel.isOpen()) {
            selector.select(); // block
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(this, key);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Nio();
    }

    private static void handleRead(Nio nio, SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int read = 0;
        StringBuilder msg = new StringBuilder();
        while ((read = channel.read(nio.buffer)) > 0) {
            nio.buffer.flip();
            while (nio.buffer.hasRemaining()) {
                msg.append((char) nio.buffer.get());
            }
            nio.buffer.clear();
        }
        String command = msg.toString().replaceAll("[\n|\r]", "");
        String[] getPath = command.split(" ");

        if (getPath[0].equals("ls")) {
            String files = Files.list(nio.serverPath)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(", "));
            files += "\n";
            channel.write(ByteBuffer.wrap(files.getBytes(StandardCharsets.UTF_8)));
        }

        if (getPath[0].equals("cd")) {
            if (getPath.length > 1 && Files.exists(Paths.get(getPath[1]), LinkOption.NOFOLLOW_LINKS) &&
                    (Files.isDirectory(Paths.get(getPath[1]), LinkOption.NOFOLLOW_LINKS))) {

                nio.serverPath = Paths.get(getPath[1]);
                channel.write(ByteBuffer.wrap(("You in: " + Paths.get(getPath[1]).toRealPath() + "\n")
                        .getBytes(StandardCharsets.UTF_8)));

                System.out.println("Path is exist");
                System.out.println("Go to: " + nio.serverPath.toRealPath().toString());
            } else {
                channel.write(ByteBuffer.wrap(("Path not exist").getBytes(StandardCharsets.UTF_8)));
            }
        }

        if (getPath[0].equals("cat")) {
            if (getPath.length > 1 && Files.exists(Paths.get(getPath[1]), LinkOption.NOFOLLOW_LINKS)) {
                System.out.println("Read file: " + Paths.get(getPath[1]).getFileName());
                System.out.println(Paths.get(getPath[1]).toAbsolutePath());
                RandomAccessFile raf = new RandomAccessFile(String.valueOf(Paths.get(getPath[1]).getFileName()), "r");
                FileChannel fileChannel = raf.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while ((fileChannel.read(buffer)) > 0) {
                    buffer.flip();
                    channel.write(buffer);
                    buffer.clear();
                }
            }
        }

        if (getPath[0].equals("touch")) {
            if (getPath.length > 1 && !Files.exists(Paths.get(getPath[1]), LinkOption.NOFOLLOW_LINKS)){
                System.out.println("Create file: " + Paths.get(getPath[1]).getFileName());
                String[] path = getPath[1].split("/");
                if (path.length == 1){
                    String pathStr = nio.serverPath.toString() + "/" + getPath[1];
                    Files.createFile(Paths.get(pathStr));
                } else {
                    Files.createFile(Paths.get(getPath[1]));
                }
            } else {
                channel.write(ByteBuffer.wrap(("File already exist").getBytes(StandardCharsets.UTF_8)));
            }
        }

        if (getPath[0].equals("mkDir")) {
            if (getPath.length > 1 && !Files.exists(Paths.get(getPath[1]), LinkOption.NOFOLLOW_LINKS)){
                System.out.println("Create Directory: " + Paths.get(getPath[1]).getFileName());
                String[] path = getPath[1].split("/");
                if (path.length == 1){
                    String pathStr = nio.serverPath.toString() + "/" + getPath[1];
                    Files.createDirectory(Paths.get(pathStr));
                } else {
                    Files.createDirectory(Paths.get(getPath[1]));
                }
            } else {
                channel.write(ByteBuffer.wrap(("Directory already exist").getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }
}