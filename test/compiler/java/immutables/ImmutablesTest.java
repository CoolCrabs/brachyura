// From https://immutables.github.io/
import org.immutables.value.Value;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ImmutablesTest {
    @Value.Immutable
    interface ValueObject {
        String name();
        List<Integer> counts();
        Optional<String> description();
    }
    
    @Value.Immutable
    @Value.Style(typeImmutable = "*", init = "set*")
    static abstract class AbstractItem {
        abstract String getName();
        abstract Set<String> getTags();
        abstract Optional<String> getDescription();
    }
    
    public static void main(String[] args) {
        ValueObject valueObject = ImmutableValueObject.builder()
            .name("My value")
            .addCounts(1)
            .addCounts(2)
            .build();
        Item namelessItem = Item.builder()
            .setName("Nameless")
            .addTags("important", "relevant")
            .setDescription("Description provided")
            .build();

        Item namedValue = namelessItem.withName("Named");
    }
}
