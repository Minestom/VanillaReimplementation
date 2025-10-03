# Codec Conversion Strategy for Legacy JSON Parsing

This document outlines the systematic approach for converting legacy JSON parsing logic to use Minecraft codecs.

## Completed Conversions

### Recipe.java
- ✅ Full codec implementation with registry-based type dispatch
- ✅ All recipe types converted (Blasting, Shaped, Shapeless, Special types, etc.)
- ✅ Backward compatibility maintained with legacy `fromJson` methods
- ✅ Comprehensive tests to ensure identical behavior

### FloatProvider.java  
- ✅ Full codec implementation with union type handling
- ✅ All provider types converted (Constant, Uniform, ClampedNormal, Trapezoid)
- ✅ Support for both direct float values and object syntax
- ✅ Comprehensive tests covering all scenarios

### JsonUtils.SingleOrList
- ✅ Enhanced with codec support for either/list patterns
- ✅ Generic codec method for reuse across types

## Conversion Pattern

The established pattern for converting legacy JSON parsing to codecs:

1. **Add Codec Imports**
   ```java
   import net.minestom.server.codec.Codec;
   import net.minestom.server.codec.StructCodec;
   import net.minestom.server.registry.DynamicRegistry;
   ```

2. **Create Main Codec Field**
   ```java
   @NotNull Codec<YourType> CODEC = makeCodec();
   
   private static StructCodec<YourType> makeCodec() {
       return Codec.RegistryTaggedUnion(registries -> {
           class Holder {
               static final @NotNull DynamicRegistry<StructCodec<? extends YourType>> CODEC = createDefaultRegistry();
           }
           return Holder.CODEC;
       }, YourType::codec, "type");
   }
   ```

3. **Implement Registry Setup**
   ```java
   private static DynamicRegistry<StructCodec<? extends YourType>> createDefaultRegistry() {
       var registry = DynamicRegistry.<StructCodec<? extends YourType>>create("your_type");
       registry.register(Key.key("minecraft:type1"), Type1.CODEC);
       registry.register(Key.key("minecraft:type2"), Type2.CODEC);
       return registry;
   }
   ```

4. **Add Codec Method to Interface**
   ```java
   @NotNull StructCodec<? extends YourType> codec();
   ```

5. **Implement Codecs for Each Type**
   ```java
   record Type1(String field1, int field2) implements YourType {
       public static final @NotNull StructCodec<Type1> CODEC = StructCodec.struct(
               "field1", Codec.STRING, Type1::field1,
               "field2", Codec.INT, Type1::field2,
               Type1::new
       );
       
       @Override
       public @NotNull StructCodec<? extends YourType> codec() {
           return CODEC;
       }
   }
   ```

6. **Maintain Backward Compatibility**
   ```java
   // Keep the legacy method
   static YourType fromJson(JsonReader reader) throws IOException {
       // ... existing implementation
   }
   ```

7. **Add Comprehensive Tests**
   - Test codec structure compiles
   - Test parsing produces identical results
   - Test all type variants
   - Test error cases

## Priority Conversion Targets

Based on analysis, these are good candidates for conversion:

1. **NumberProvider** - Similar pattern to FloatProvider, but with Int/Double variants
2. **HeightProvider** - Union types with VerticalAnchor dependency  
3. **DensityFunction** - Complex type system with many variants
4. **Noise** - Relatively simple with a few types
5. **Biome** - Complex but well-structured

## Implementation Notes

- Always preserve backward compatibility during transition
- Use `orElse()` for fallback handling (e.g., raw numbers vs objects)
- Leverage existing codec patterns from loot-table module
- Test extensively to ensure identical behavior
- Document conversion strategy and patterns

## Next Steps

1. Continue converting remaining types using the established pattern
2. Implement actual codec-based parsing integration with Minestom's codec system
3. Gradually replace legacy `JsonUtils.unionStringTypeAdapted` calls
4. Update dependent classes to use new codec-based parsing
5. Remove legacy parsing code once transition is complete

## Testing Strategy

Each conversion should include:
- Codec structure compilation tests
- Behavior compatibility tests comparing legacy vs codec results
- Edge case handling tests
- Error condition tests
- Performance comparison tests (optional)