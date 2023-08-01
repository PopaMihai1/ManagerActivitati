import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class EvidentaActivitati {

    private static final String URL = "jdbc:mysql://localhost:3306/baza_de_date"; // Modifică "nume_baza_de_date" cu numele bazei de date creată în XAMPP
    private static final String USER = "root"; // Modifică "nume_utilizator" cu numele de utilizator al bazei de date
    private static final String PASSWORD = ""; // Modifică "parola" cu parola utilizatorului bazei de date

    private static final String TABLE_NAME = "activitati";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NUME = "nume";
    private static final String COLUMN_EXECUTATA = "executata";

    private static ArrayList<Activitate> activitati = new ArrayList<>();
    private static ArrayList<Activitate> activitatiSterse = new ArrayList<>();

    private static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        creareTabela();
        incarcaActivitati();

        int optiune;

        do {
            afisareMeniu();
            optiune = scanner.nextInt();

            switch (optiune) {
                case 1:
                    adaugaActivitate();
                    break;
                case 2:
                    afisareActivitati();
                    break;
                case 3:
                    afisareActivitatiExecutate();
                    break;
                case 4:
                    marcheazaActivitateExecutata();
                    break;
                case 5:
                    afisareTermeneLimita();
                    break;
                case 6:
                    stergeActivitate();
                    break;
                case 7:
                    afisareActivitatiSterse();
                    break;
                case 8:
                    salvareActivitati();
                    System.out.println("La revedere!");
                    break;
                default:
                    System.out.println("Optiune invalida! Alege din nou.");
            }

        } while (optiune != 8);
    }

    private static void afisareMeniu() {
        System.out.println("===== Meniu =====");
        System.out.println("1. Introdu activitate");
        System.out.println("2. Lista cu activitati");
        System.out.println("3. Activitati executate");
        System.out.println("4. Marcheaza activitate ca executata");
        System.out.println("5. Lista cu termene limita");
        System.out.println("6. Sterge activitate");
        System.out.println("7. Activitati sterse");
        System.out.println("8. Iesire din aplicatie");
        System.out.print("Alege o optiune: ");
    }



    private static void creareTabela() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_ID + " INT AUTO_INCREMENT PRIMARY KEY, " +
                    COLUMN_NUME + " TEXT NOT NULL, " +
                    COLUMN_EXECUTATA + " BOOLEAN NOT NULL)";
            statement.execute(createTableQuery);

        } catch (SQLException e) {
            System.err.println("Eroare la crearea tabelei: " + e.getMessage());
        }
    }

    private static void incarcaActivitati() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            String selectQuery = "SELECT * FROM " + TABLE_NAME;
            ResultSet resultSet = statement.executeQuery(selectQuery);

            while (resultSet.next()) {
                int id = resultSet.getInt(COLUMN_ID);
                String nume = resultSet.getString(COLUMN_NUME);
                boolean executata = resultSet.getBoolean(COLUMN_EXECUTATA);
                Activitate activitate = new Activitate(id, nume, executata);
                activitati.add(activitate);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la incarcarea activitatilor: " + e.getMessage());
        }
    }

    private static void adaugaActivitate() {
        System.out.print("Introdu numele activitatii: ");
        scanner.nextLine();
        String numeActivitate = scanner.nextLine();

        System.out.print("Introdu termenul limita al activitatii: ");
        String termenLimita = scanner.nextLine();

        Activitate activitate = new Activitate(numeActivitate, false, termenLimita);
        activitati.add(activitate);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO " + TABLE_NAME + " (" + COLUMN_NUME + ", " + COLUMN_EXECUTATA + ") VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, numeActivitate);
            preparedStatement.setBoolean(2, false);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                activitate.setId(id);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea activitatii: " + e.getMessage());
        }

        System.out.println("Activitate adaugata cu succes!");
    }

    private static void stergeActivitate() {
        System.out.print("Introdu numarul activitatii pe care doresti sa o stergi: ");
        int index = scanner.nextInt() - 1;
        if (index >= 0 && index < activitati.size()) {
            Activitate activitate = activitati.get(index);
            int id = activitate.getId();

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?")) {

                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Eroare la stergerea activitatii: " + e.getMessage());
            }

            activitatiSterse.add(activitate); // Mutăm activitatea în lista de activități șterse
            activitati.remove(index);
            System.out.println("Activitatea a fost stearsa cu succes!");
        } else {
            System.out.println("Numarul activitatii introdus este invalid!");
        }
    }

    private static void afisareTermeneLimita() {
        System.out.println("===== Lista cu termene limita =====");
        if (activitati.isEmpty()) {
            System.out.println("Lista de activitati este goala.");
        } else {
            for (int i = 0; i < activitati.size(); i++) {
                Activitate activitate = activitati.get(i);
                System.out.println((i + 1) + ". " + activitate.getNume() + " - Termen limita: " + activitate.getTermenLimita());
            }
        }
    }


    private static void afisareActivitati() {
        System.out.println("===== Lista cu activitati =====");
        if (activitati.isEmpty()) {
            System.out.println("Lista de activitati este goala.");
        } else {
            for (int i = 0; i < activitati.size(); i++) {
                Activitate activitate = activitati.get(i);
                System.out.println((i + 1) + ". " + activitate.getNume() + " - " + (activitate.isExecutata() ? "Executata" : "Neexecutata"));
            }
        }
    }

    private static void afisareActivitatiExecutate() {
        System.out.println("===== Activitati executate =====");
        boolean existaActivitatiExecutate = false;
        for (Activitate activitate : activitati) {
            if (activitate.isExecutata()) {
                System.out.println("- " + activitate.getNume());
                existaActivitatiExecutate = true;
            }
        }
        if (!existaActivitatiExecutate) {
            System.out.println("Nu exista activitati executate.");
        }
    }


    private static void afisareActivitatiSterse() {
        System.out.println("===== Activitati sterse =====");
        if (activitatiSterse.isEmpty()) {
            System.out.println("Nu exista activitati sterse.");
        } else {
            for (int i = 0; i < activitatiSterse.size(); i++) {
                Activitate activitate = activitatiSterse.get(i);
                System.out.println((i + 1) + ". " + activitate.getNume() + " - " + (activitate.isExecutata() ? "Executata" : "Neexecutata") + " - Termen limita: " + activitate.getTermenLimita());
            }
        }
    }

    private static void marcheazaActivitateExecutata() {
        System.out.print("Introdu numarul activitatii pe care doresti sa o marchezi ca executata: ");
        int index = scanner.nextInt() - 1;
        if (index >= 0 && index < activitati.size()) {
            Activitate activitate = activitati.get(index);
            activitate.setExecutata(true);

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "UPDATE " + TABLE_NAME + " SET " + COLUMN_EXECUTATA + " = ? WHERE " + COLUMN_ID + " = ?")) {

                preparedStatement.setBoolean(1, true);
                preparedStatement.setInt(2, activitate.getId());
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Eroare la actualizarea starii activitatii: " + e.getMessage());
            }

            System.out.println("Activitatea a fost marcata ca executata!");
        } else {
            System.out.println("Numarul activitatii introdus este invalid!");
        }
    }

    private static void salvareActivitati() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            String deleteQuery = "DELETE FROM " + TABLE_NAME;
            statement.executeUpdate(deleteQuery);

            for (Activitate activitate : activitati) {
                String insertQuery = "INSERT INTO " + TABLE_NAME + " (" + COLUMN_NUME + ", " + COLUMN_EXECUTATA + ") " +
                        "VALUES ('" + activitate.getNume() + "', " + (activitate.isExecutata() ? "1" : "0") + ")";
                statement.executeUpdate(insertQuery);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la salvarea activitatilor: " + e.getMessage());
        }
    }

    private static class Activitate {
        private int id;
        private String nume;
        private boolean executata;

        public Activitate(int id, String nume, boolean executata) {
            this.id = id;
            this.nume = nume;
            this.executata = executata;
        }

        private String termenLimita;

        public Activitate(int id, String nume, boolean executata, String termenLimita) {
            this.id = id;
            this.nume = nume;
            this.executata = executata;
            this.termenLimita = termenLimita;
        }

        public Activitate(String nume, boolean executata, String termenLimita) {
            this.nume = nume;
            this.executata = executata;
            this.termenLimita = termenLimita;
        }

        public String getTermenLimita() {
            return termenLimita;
        }

        public void setTermenLimita(String termenLimita) {
            this.termenLimita = termenLimita;
        }


        public Activitate(String nume, boolean executata) {
            this.nume = nume;
            this.executata = executata;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNume() {
            return nume;
        }

        public boolean isExecutata() {
            return executata;
        }

        public void setExecutata(boolean executata) {
            this.executata = executata;
        }
    }
}
