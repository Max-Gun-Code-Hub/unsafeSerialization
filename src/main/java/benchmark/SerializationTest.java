package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import weights.EnchantedWeightSet;
import weights.RegularWeightsSet;
import weights.WeightsSet;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Thread)
public class SerializationTest {
    public static int LONGS_SIZE = 100_000;

    RegularWeightsSet regularWeightsSet = null;
    EnchantedWeightSet enchantedWeightSet = null;

    byte[] regularSerializedWeightsSet = null;
    byte[] enchantedSerializedWeightSet = null;

    public static void main(String[] args) {
        try {
            runTests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runTests() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SerializationTest.class.getName())
                .warmupIterations(5) //5
                .measurementIterations(10) //10
                .forks(1)
                .jvmArgsAppend("-XX:CompileThreshold=3")
                .jvmArgsAppend("-XX:+UseCompressedOops")
                .build();

        new Runner(opt).run();
    }

    public SerializationTest() {
        setup();
    }

    // The regular "lame" serialization
    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testRegularSerialization( ) {
        testSerialization(regularWeightsSet);
    }

    // The cool serialization
    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testMagicalSerialization() {
        testSerialization(enchantedWeightSet);
    }

    // The regular "lame" deserialization
    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testRegularDeserialization( ) {
        testDeserialization(regularSerializedWeightsSet);
    }

    // The cool deserialization
    @Benchmark
    @BenchmarkMode(value = Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testMagicalDeserialization() {
        testDeserialization(enchantedSerializedWeightSet);
    }

    public void testSerialization(WeightsSet weightsSet) {
        try {
            // Serialize data object to byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;

            out.writeObject(weightsSet);
            byte[] result = bos.toByteArray();

            out.close();

            if (result == null) {
                // this statement is to "trick" the optimizer
                throw new IllegalStateException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testDeserialization(byte[] serializedWeightsSet) {
        // Deserialize data object from a byte[]
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(serializedWeightsSet);
            ObjectInputStream reader = new ObjectInputStream(bis);
            WeightsSet result = (WeightsSet) reader.readObject();

            reader.close();

            if (result == null) {
                // this statement is to "trick" the optimizer
                throw new IllegalStateException();
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void setup() {
        Random random = new Random();

        // Creating & Initializing long[] & byte[]
        long[] longs = new long[LONGS_SIZE];
        byte[] bytes = new byte[LONGS_SIZE * 8];

        IntStream.range(0, LONGS_SIZE).forEach(i -> longs[i] = random.nextLong());
        random.nextBytes(bytes);

        // Instantiating our test objects
        regularWeightsSet = new RegularWeightsSet(Arrays.copyOf(longs, LONGS_SIZE));
        enchantedWeightSet = new EnchantedWeightSet(Arrays.copyOf(longs, LONGS_SIZE));

        try {
            ByteArrayOutputStream regularBos = new ByteArrayOutputStream() ;
            ObjectOutputStream regularOut = new ObjectOutputStream(regularBos) ;

            regularOut.writeObject(regularWeightsSet);
            regularSerializedWeightsSet = regularBos.toByteArray();

            ByteArrayOutputStream enchantedBos = new ByteArrayOutputStream() ;
            ObjectOutputStream enchantedOut = new ObjectOutputStream(enchantedBos) ;

            enchantedOut.writeObject(enchantedWeightSet);
            enchantedSerializedWeightSet = enchantedBos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
