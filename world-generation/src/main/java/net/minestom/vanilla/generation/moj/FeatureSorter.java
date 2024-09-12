package net.minestom.vanilla.generation.moj;

import it.unimi.dsi.fastutil.objects.*;
import net.minestom.vanilla.datapack.worldgen.PlacedFeature;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class FeatureSorter {
    record FeatureData(int featureIndex, int step, PlacedFeature feature) {

        public int featureIndex() {
            return this.featureIndex;
        }

        public int step() {
            return this.step;
        }

        public PlacedFeature feature() {
            return this.feature;
        }
    }

    public static <T> List<StepFeatureData> buildFeaturesPerStep(List<T> elements, Function<T, List<Set<PlacedFeature>>> featureExtractor, boolean allowCycles) {
        Object2IntMap<PlacedFeature> featureIndexMap = new Object2IntOpenHashMap<>();
        AtomicInteger featureIndexCounter = new AtomicInteger();
        Comparator<FeatureData> featureDataComparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        Map<FeatureData, Set<FeatureData>> featureGraph = new TreeMap<>(featureDataComparator);
        int maxSteps = 0;

        ArrayList<FeatureData> currentFeatures;
        int stepIndex;

        for (T element : elements) {
            currentFeatures = new ArrayList<>();
            List<Set<PlacedFeature>> stepFeatures = featureExtractor.apply(element);
            maxSteps = Math.max(maxSteps, stepFeatures.size());

            for (stepIndex = 0; stepIndex < stepFeatures.size(); ++stepIndex) {
                for (PlacedFeature feature : stepFeatures.get(stepIndex)) {
                    currentFeatures.add(new FeatureData(featureIndexMap.computeIfAbsent(feature, (featureKey) -> {
                        return featureIndexCounter.getAndIncrement();
                    }), stepIndex, feature));
                }
            }

            for (stepIndex = 0; stepIndex < currentFeatures.size(); ++stepIndex) {
                Set<FeatureData> nextFeatures = featureGraph.computeIfAbsent(currentFeatures.get(stepIndex), (featureDataKey) -> new TreeSet<>(featureDataComparator));
                if (stepIndex < currentFeatures.size() - 1) {
                    nextFeatures.add(currentFeatures.get(stepIndex + 1));
                }
            }
        }

        Set<FeatureData> visitedFeatures = new TreeSet<>(featureDataComparator);
        Set<FeatureData> inProgressFeatures = new TreeSet<>(featureDataComparator);
        currentFeatures = new ArrayList<>();

        for (FeatureData featureData : featureGraph.keySet()) {
            if (!inProgressFeatures.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }

            if (visitedFeatures.contains(featureData)) {
                continue;
            }

            Objects.requireNonNull(currentFeatures);
            if (!DepthFirstSearch.depthFirstSearch(featureGraph, visitedFeatures, inProgressFeatures, currentFeatures::add, featureData)) {
                continue;
            }

            if (!allowCycles) {
                throw new IllegalStateException("Feature order cycle found");
            }

            List<T> remainingElements = new ArrayList<>(elements);
            int remainingSize;
            do {
                remainingSize = remainingElements.size();
                ListIterator<T> elementIterator = remainingElements.listIterator();

                while (elementIterator.hasNext()) {
                    T currentElement = elementIterator.next();
                    elementIterator.remove();

                    try {
                        buildFeaturesPerStep(remainingElements, featureExtractor, false);
                    } catch (IllegalStateException e) {
                        continue;
                    }

                    elementIterator.add(currentElement);
                }
            } while (remainingSize != remainingElements.size());

            throw new IllegalStateException("Feature order cycle found, involved sources: " + remainingElements);
        }

        Collections.reverse(currentFeatures);
        List<StepFeatureData> stepFeatureDataList = new ArrayList<>();

        for (stepIndex = 0; stepIndex < maxSteps; ++stepIndex) {
            int finalStepIndex = stepIndex;
            List<PlacedFeature> stepFeaturesList = currentFeatures.stream()
                    .filter((featureData) -> featureData.step() == finalStepIndex)
                    .map(FeatureData::feature)
                    .collect(Collectors.toList());
            stepFeatureDataList.add(new StepFeatureData(stepFeaturesList));
        }

        return List.copyOf(stepFeatureDataList);
    }

    public record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
        StepFeatureData(List<PlacedFeature> features) {
            this(features, createIndexIdentityLookup(features));
        }

        public List<PlacedFeature> features() {
            return this.features;
        }

        public ToIntFunction<PlacedFeature> indexMapping() {
            return this.indexMapping;
        }
    }

    private static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> elements) {
        int size = elements.size();
        if (size < 8) {
            ReferenceList<T> referenceList = new ReferenceImmutableList<>(elements);
            Objects.requireNonNull(referenceList);
            return referenceList::indexOf;
        } else {
            Reference2IntMap<T> referenceMap = new Reference2IntOpenHashMap<>(size);
            referenceMap.defaultReturnValue(-1);

            for (int index = 0; index < size; ++index) {
                referenceMap.put(elements.get(index), index);
            }
            return referenceMap;
        }
    }
}
