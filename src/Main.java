import java.util.Random;
import java.util.concurrent.Semaphore;

class Restaurante {
    private int pizzasVendidas = 0;
    private int bocadillosVendidos = 0;
    private double dineroRecaudado = 0;
    private Semaphore semaforoPizza = new Semaphore(0);
    private Semaphore semaforoBocadillo = new Semaphore(0);
    private volatile boolean abierto = true;

    public void venderPizza() {
        pizzasVendidas++;
        dineroRecaudado += 12;
    }

    public void venderBocadillo() {
        bocadillosVendidos++;
        dineroRecaudado += 6;
    }

    public void cerrar() {
        abierto = false;
        System.out.println("Cerrando el restaurante...");
        System.out.println("Pizzas vendidas: " + pizzasVendidas);
        System.out.println("Bocadillos vendidos: " + bocadillosVendidos);
        System.out.println("Dinero recaudado: " + dineroRecaudado + "€");
    }

    public boolean estaAbierto() {
        return abierto;
    }

    public Semaphore getSemaforoPizza() {
        return semaforoPizza;
    }

    public Semaphore getSemaforoBocadillo() {
        return semaforoBocadillo;
    }
}

class Pizzero implements Runnable {
    private Restaurante restaurante;

    public Pizzero(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    @Override
    public void run() {
        while (restaurante.estaAbierto()) {
            try {
                // Estirar masa
                System.out.println("Pizzero: Estirando masa...");
                Thread.sleep(2000);
                // Poner ingredientes
                System.out.println("Pizzero: Poniendo ingredientes...");
                Thread.sleep(1000);
                // Cocinar pizza
                System.out.println("Pizzero: Cocinando pizza...");
                Thread.sleep(5000);
                // Incrementar disponible
                restaurante.getSemaforoPizza().release();
                restaurante.venderPizza();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class Bocatero implements Runnable {
    private Restaurante restaurante;

    public Bocatero(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    @Override
    public void run() {
        while (restaurante.estaAbierto()) {
            try {
                // Cortar pan
                System.out.println("Bocatero: Cortando pan...");
                Thread.sleep(1000);
                // Poner mayonesa
                System.out.println("Bocatero: Poniendo mayonesa...");
                Thread.sleep(1000);
                // Poner ingredientes
                System.out.println("Bocatero: Poniendo ingredientes...");
                Thread.sleep(2000);
                // Envolver bocadillo
                System.out.println("Bocatero: Envolviendo bocadillo...");
                Thread.sleep(3000);
                // Incrementar disponible
                restaurante.getSemaforoBocadillo().release();
                restaurante.venderBocadillo();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class Cliente implements Runnable {
    private Restaurante restaurante;

    public Cliente(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    @Override
    public void run() {
        try {
            // Decidir qué comer
            Thread.sleep(10000); // Tarda 10 segundos en decidir
            Random rand = new Random();
            boolean quierePizza = rand.nextBoolean();
            int cantidad = rand.nextInt(4) + 1; // Cantidad entre 1 y 4
            System.out.println("Cliente: Quiere " + (quierePizza ? cantidad + " pizzas." : cantidad + " bocadillos."));

            if (quierePizza) {
                for (int i = 0; i < cantidad; i++) {
                    restaurante.getSemaforoPizza().acquire();
                }
            } else {
                for (int i = 0; i < cantidad; i++) {
                    restaurante.getSemaforoBocadillo().acquire();
                }
            }
            System.out.println("Cliente: Ha terminado de recoger. Se va.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Restaurante restaurante = new Restaurante();
        Thread pizzero = new Thread(new Pizzero(restaurante));
        Thread bocatero = new Thread(new Bocatero(restaurante));

        pizzero.start();
        bocatero.start();

        int numClientes = 5; // Cambia este número para probar diferentes cantidades de clientes
        Thread[] clientes = new Thread[numClientes];
        for (int i = 0; i < numClientes; i++) {
            clientes[i] = new Thread(new Cliente(restaurante));
            clientes[i].start();
        }

        // Esperar a que todos los clientes terminen
        for (Thread cliente : clientes) {
            cliente.join();
        }

        // Cerrar el restaurante
        restaurante.cerrar();
        pizzero.interrupt();
        bocatero.interrupt();
        pizzero.join();
        bocatero.join();
    }
}
