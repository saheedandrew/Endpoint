import java.util.*;

class InvalidCommandException extends Exception {
    public InvalidCommandException(String message) {
        super(message);
    }
}

class DirectoryException extends Exception {
    public DirectoryException(String message) {
        super(message);
    }
}

class Directory {
    String name;
    Directory parent;
    Map<String, Directory> children;

    public Directory(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
        this.children = new TreeMap<>();
    }

    public void addChild(Directory child) {
        children.put(child.name, child);
    }

    public Directory getChild(String name) {
        if (hasChildren()){
            return children.get(name);
        }
        return null;
    }

    public void removeChild(String name) {
        children.remove(name);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

}

class DirectoryManager {
    private final Directory root;
    private static final String CREATE = "CREATE";
    private static final String MOVE = "MOVE";
    private static final String DELETE = "DELETE";
    private static final String LIST = "LIST";

    public DirectoryManager() {
        this.root = new Directory("", null);
    }

    public void executeCommand(String command) throws InvalidCommandException, DirectoryException {
        String[] parts = command.split(" ", 3);
        if (parts.length == 0) {
            throw new InvalidCommandException("Empty command");
        }

        String action = parts[0];
        String path = parts.length > 1 ? parts[1] : "";

        switch (action) {
            case CREATE:
                createDirectory(path);
                break;
            case MOVE:
                if (parts.length < 3) {
                    throw new InvalidCommandException("Invalid MOVE command: " + command);
                }
                String sourcePath = parts[1];
                String destPath = parts[2];
                moveDirectory(sourcePath, destPath);
                break;
            case DELETE:
                deleteDirectory(path);
                break;
            case LIST:
                listDirectories();
                break;
            default:
                throw new InvalidCommandException("Unsupported command: " + command);
        }
    }

    private void createDirectory(String path) throws DirectoryException {
        if (path == null || path.isEmpty()) {
            throw new DirectoryException("Invalid path: " + path);
        }

        String[] dirs = path.split("/");
        Directory current = root;

        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            if (!current.children.containsKey(dir)) {
                Directory newDir = new Directory(dir, current);
                current.addChild(newDir);
            }
            current = current.getChild(dir);
        }
        System.out.println("CREATE " + path);
    }

    private void moveDirectory(String sourcePath, String destPath) throws DirectoryException {
        Directory sourceDir = getDirectory(sourcePath);
        Directory destDir = getDirectory(destPath);

        if (sourceDir == null) {
            throw new DirectoryException("Cannot move " + sourcePath + " - source does not exist");
        }
        if (destDir == null) {
            throw new DirectoryException("Cannot move " + sourcePath + " - destination does not exist");
        }

        sourceDir.parent.removeChild(sourceDir.name);

        destDir.addChild(sourceDir);
        sourceDir.parent = destDir;

        System.out.println("MOVE " + sourcePath + " " + destPath);
    }

    private void deleteDirectory(String path) throws DirectoryException {
        Directory dir = getDirectory(path);
        System.out.println(DELETE +" " + path);
        if (dir == null) {
            System.out.println("Cannot delete " + path + " - does not exist");
        }else{
            dir.parent.removeChild(dir.name);
        }
    }

    private void listDirectories() {
        System.out.println(LIST);
        listDirectory(root, 0);
    }

    private void listDirectory(Directory dir, int depth) {
        if (dir != root) {
            System.out.println("  ".repeat(depth) + dir.name);
        }
        for (Directory child : dir.children.values()) { // TreeMap ensures alphabetical order
            listDirectory(child, depth + 1);
        }
    }

    private Directory getDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] dirs = path.split("/");
        Directory current = root;
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            current = current.getChild(dir);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}

public class Main {
    public static void main(String[] args) {
        DirectoryManager manager = new DirectoryManager();
        String[] commands = {
                "CREATE fruits",
                "CREATE vegetables",
                "CREATE grains",
                "CREATE fruits/apples",
                "CREATE fruits/apples/fuji",
                "LIST",
                "CREATE grains/squash",
                "MOVE grains/squash vegetables",
                "CREATE foods",
                "MOVE grains foods",
                "MOVE fruits foods",
                "MOVE vegetables foods",
                "LIST",
                "DELETE fruits/apples",
                "DELETE foods/fruits/apples",
                "LIST"
        };

        for (String command : commands) {
            try {
                manager.executeCommand(command);
            } catch (InvalidCommandException | DirectoryException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}