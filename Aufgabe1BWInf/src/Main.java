import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        //args = new String[] {"6"};
        runGenerator(args);
        output(runGenerator(args));
    }

    public static String runGenerator(String[] args) {
        // Das Argument, das dem Programm zu Beginn übergeben wird, gibt die Größe des Arukones an.
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing argument: Size of arukone-field");
        }
        int size;
        try {
            size = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // Falls das übergebene Argument keine Zahl ist.
            throw new IllegalArgumentException("Wrong Datatype: Size must be a number");
        }
        if (size < 4) {
            // Falls die Zahl kleiner als vier ist.
            throw new IllegalArgumentException("Size has to be greater than four");
        }

        // Das paths-Array speichert die Pfade, die generiert werden.
        int[][] paths = new int[size][size];

        // In unserem Fall gibt es immer so viele Zahlenpaare, wie das Feld groß ist.
        int numbers = size;
        // In currentPos werden die aktuellen X- und Y-Koordinaten zwischengespeichert.
        int[][] currentPos = new int[numbers][2];
        // In startPos werden die Startpositionen der einzelnen Pfade gespeichert.
        int[][] startPos = new int[numbers][2];

        // Nun werden die zufälligen Startpositionen generiert.
        Random random = new Random();
        for (int i = 0; i < numbers; i++) {
            int x, y;
            do {
                x = random.nextInt(0, size);
                y = random.nextInt(0, size);
                // Ist das Feld bereits besetzt, wird neu generiert.
            } while (paths[x][y] != 0);

            // Die entsprechende Stelle wird im paths-Array markiert, ...
            paths[x][y] = i + 1;
            /// ... und die aktuellen Koordinaten werden in currentPos und startPos eingetragen.
            currentPos[i][0] = x;
            currentPos[i][1] = y;
            startPos[i][0] = x;
            startPos[i][1] = y;
        }

        // In directions wird die aktuelle Richtung des Pfades gespeichert.
        int[] directions = new int[numbers];
        // In iterations wird die bisherige Anzahl an Iterationen pro Pfad getrackt,
        // wenn diese limit überschreitet wird abgebrochen.
        int[] iterations = new int[numbers];
        final int limit = (int)(size * 1.0f);

        // In isStuck wird für jeden einzelnen Pfad gespeichert, ob er noch fortgeführt werden kann
        Byte[] isStuck = new Byte[numbers]; // 0: frei, 1: ein Ende ist blockiert, 2: beide Enden sind blockiert
        Arrays.fill(isStuck, (byte) 0);

        List<Integer> possibleDirections = new ArrayList<>(List.of(1, 2, 3, 4));

        int j = size * size;
        // j = Anzahl an Feldern
        while (isAnyStuck(isStuck) && j > 0) {
            // Solange nicht alle Pfade blockiert sind und es noch genug freie Felder gibt.
            j--;

            for (int i = 0; i < numbers; i++) {
                // Iteriere über alle Pfade
                if (isStuck[i]==2) continue;
                if (iterations[i] > limit) continue;
                // wenn der Pfad die maximale Anzahl an Iterationen überschritten hat

                // Wenn das eine Ende blockiert ist, wird am anderen weitergemacht
                int[] pos = isStuck[i] == 0 ? new int[] {currentPos[i][0], currentPos[i][1]} : new int[] {startPos[i][0], startPos[i][1]};

                int direction = directions[i];
                if (direction == 0) {
                    Collections.shuffle(possibleDirections);
                    boolean stuck = true;
                    for (Integer newDirection: possibleDirections) {
                        int[] newPos = goInDirection(pos, newDirection);
                        if (isValid(newPos, pos, paths)) {
                            stuck = false;
                            pos = newPos;
                            direction = newDirection;
                            break;
                        }
                    }
                    if (stuck) {
                        return runGenerator(args);
                    }
                } else {
                    pos = goInDirection(pos, direction);
                    int[] oldPos = isStuck[i] == 0 ? currentPos[i] : startPos[i];
                    if (!isValid(pos, oldPos, paths)) {
                        boolean stuck = true;
                        Collections.shuffle(possibleDirections);
                        for (Integer newDirection: possibleDirections) {
                            if (newDirection == direction) continue;
                            int[] newPos = goInDirection(oldPos, newDirection);
                            if (isValid(newPos, oldPos, paths)) {
                                pos = newPos;
                                direction = newDirection;
                                stuck = false;
                                break;
                            }
                        }
                        if (stuck) {
                            isStuck[i]++;
                            continue;
                        }
                        if (isStuck[i] == 2) continue;
                    }
                }
                if (isStuck[i] == 0) currentPos[i] = pos;
                else startPos[i] = pos;

                directions[i] = direction;
                paths[pos[0]][pos[1]] = i+1;
                iterations[i]++;
            }
        }
        // Im board-Array werden die daten gespeichert, die später als das Arukone ausgegeben werden
        int[][] board = new int[size][size];

        for (int i = 0; i < numbers; i++) {
            // Anfangs- und Endpositionen der Pfade werden ins board-Array übertragen.
            board[currentPos[i][0]][currentPos[i][1]] = i + 1;
            board[startPos[i][0]][startPos[i][1]] = i + 1;
        }

        StringBuilder out = new StringBuilder();
        out.append(size).append("\n");
        out.append(numbers).append("\n");
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                out.append(board[x][y]).append(" ");
            }
            out.append("\n");
        }
        return out.toString();
    }

    private static boolean isAnyStuck(Byte[] isStuck) {
        return Arrays.stream(isStuck).anyMatch(stuck -> stuck != 2);
    }

    private static int[] goInDirection(int[] pos, int direction) {
        int x = pos[0], y = pos[1];
        switch (direction) {
            case 1 -> x--;
            case 2 -> y--;
            case 3 -> x++;
            case 4 -> y++;
            default -> System.err.println("Wrong direction: " + direction + " " + x + " " + y);
        }
        return new int[] {x, y};
    }

    private static boolean isValid(int[] pos, int[] oldPos, int[][] paths) {
        int size = paths.length;
        int x = pos[0], y = pos[1];
        if (x < 0 || y < 0 || x >= size || y >= size) {
            return false;
        }
        return paths[x][y] == 0 && !hasAdjacent(pos, oldPos, paths);
    }

    private static void output(String out) {
        String fileName = "output.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(out);
            System.out.println("Text file created successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private static boolean hasAdjacent(int[] pos, int[] oldPos, int[][] paths) {
        // Prüft, ob sich in den benachbarten Feldern von pos mit Ausnahme von oldPos ei
        int number = paths[oldPos[0]][oldPos[1]];
        for (int i = 0; i < 4; i++) {
            int[] newPos = goInDirection(pos, i+1);
            int newX = newPos[0], newY = newPos[1];
            if (newX == oldPos[0] && newY == oldPos[1]) continue;
            if (!isInBounds(newX, newY, paths)) continue;
            if (paths[newX][newY] == number) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInBounds(int x, int y, int[][] paths) {
        int size = paths.length;
        return x >= 0 && y >= 0 && x < size && y < size;
    }
}
