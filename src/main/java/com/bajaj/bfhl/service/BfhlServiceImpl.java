package com.bajaj.bfhl.service;

import com.bajaj.bfhl.dto.BfhlRequest;
import com.bajaj.bfhl.dto.BfhlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BfhlServiceImpl implements BfhlService {

    private static final Logger log = LoggerFactory.getLogger(BfhlServiceImpl.class);
    private static final Set<Character> VOWELS = Set.of('A', 'E', 'I', 'O', 'U');

    @Override
    public BfhlResponse process(BfhlRequest request, String requestId) {
        long startTime = System.currentTimeMillis();

        log.info("Processing BFHL request. requestId={}, elementCount={}", requestId,
                request.getData() == null ? 0 : request.getData().size());

        List<Object> rawData = request.getData() == null ? Collections.emptyList() : request.getData();
        int totalReceived = rawData.size();

        // 1. Sanitise: remove null / blank strings
        List<String> sanitised = rawData.stream()
                .filter(o -> o != null)
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        int invalidIgnored = totalReceived - sanitised.size();

        // 2. Detect duplicates before de-duplication
        Set<String> seen = new LinkedHashSet<>();
        boolean containsDuplicates = false;
        for (String s : sanitised) {
            if (!seen.add(s)) containsDuplicates = true;
        }
        List<String> unique = new ArrayList<>(seen);
        int validProcessed = unique.size();

        // 3. Categorise each unique element
        List<BigDecimal> numericValues = new ArrayList<>();
        List<String> alphabeticTokens  = new ArrayList<>();
        List<String> specialChars      = new ArrayList<>();

        for (String token : unique) {
            if (isPureNumber(token)) {
                numericValues.add(new BigDecimal(token));
            } else if (isPureAlpha(token)) {
                alphabeticTokens.add(token.toUpperCase());
            } else if (isSpecialCharacter(token)) {
                specialChars.add(token);
            } else {
               extractAlphanumericLettersOnly(token, alphabeticTokens);
            }
        }

        // 4. Odd / Even
        List<String> oddNumbers  = new ArrayList<>();
        List<String> evenNumbers = new ArrayList<>();
        for (BigDecimal n : numericValues) {
            if (isDecimal(n)) continue;
            if (n.longValueExact() % 2 == 0) evenNumbers.add(formatNumber(n));
            else oddNumbers.add(formatNumber(n));
        }

        // 5. Sum
        BigDecimal sum = numericValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Largest / Smallest
        String largestNumber  = null;
        String smallestNumber = null;
        if (!numericValues.isEmpty()) {
            largestNumber  = formatNumber(Collections.max(numericValues));
            smallestNumber = formatNumber(Collections.min(numericValues));
        }

        // 7. Sorted numbers
        List<String> sortedNumbers = numericValues.stream()
                .sorted().map(this::formatNumber).collect(Collectors.toList());

        // 8. Alphabet frequency
        Map<String, Integer> alphabetFrequency = new TreeMap<>();
        for (String token : alphabeticTokens) {
            for (char c : token.toCharArray()) {
                String key = String.valueOf(c).toUpperCase();
                alphabetFrequency.merge(key, 1, Integer::sum);
            }
        }

        // 9. Vowel / Consonant count
        int vowelCount = 0, consonantCount = 0;
        for (String token : alphabeticTokens) {
            for (char c : token.toCharArray()) {
                if (VOWELS.contains(Character.toUpperCase(c))) vowelCount++;
                else consonantCount++;
            }
        }

        // 10. Longest / Shortest alpha string
        List<String> pureAlphaTokens = unique.stream()
                .filter(this::isPureAlpha)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        String longestAlphabeticValue  = null;
        String shortestAlphabeticValue = null;
        if (!pureAlphaTokens.isEmpty()) {
            longestAlphabeticValue  = pureAlphaTokens.stream().max(Comparator.comparingInt(String::length)).orElse(null);
            shortestAlphabeticValue = pureAlphaTokens.stream().min(Comparator.comparingInt(String::length)).orElse(null);
        }

        // 11. Alphabets list
        List<String> alphabetsList = buildAlphabetsList(unique);

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("BFHL processing complete. requestId={}, processingTimeMs={}", requestId, processingTime);

        return BfhlResponse.builder()
                .isSuccess(true)
                .requestId(requestId)
                .oddNumbers(oddNumbers)
                .evenNumbers(evenNumbers)
                .alphabets(alphabetsList)
                .specialCharacters(specialChars)
                .sum(formatNumber(sum))
                .largestNumber(largestNumber)
                .smallestNumber(smallestNumber)
                .alphabetCount(alphabeticTokens.stream().mapToInt(String::length).sum())
                .numberCount(numericValues.size())
                .specialCharacterCount(specialChars.size())
                .containsDuplicates(containsDuplicates)
                .uniqueElementCount(validProcessed)
                .sortedNumbers(sortedNumbers)
                .vowelCount(vowelCount)
                .consonantCount(consonantCount)
                .alphabetFrequency(alphabetFrequency.isEmpty() ? null : alphabetFrequency)
                .longestAlphabeticValue(longestAlphabeticValue)
                .shortestAlphabeticValue(shortestAlphabeticValue)
                .processingTimeMs(processingTime)
                .summary(BfhlResponse.Summary.builder()
                        .totalElementsReceived(totalReceived)
                        .validElementsProcessed(validProcessed)
                        .invalidElementsIgnored(invalidIgnored)
                        .build())
                .build();
    }

    private boolean isPureNumber(String token) {
        try { new BigDecimal(token); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isPureAlpha(String token) {
        return token.chars().allMatch(Character::isLetter);
    }

    private boolean isSpecialCharacter(String token) {
        if (token.length() != 1) return false;
        char c = token.charAt(0);
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private boolean isDecimal(BigDecimal n) {
        return n.scale() > 0 && n.stripTrailingZeros().scale() > 0;
    }

    private void extractAlphanumeric(String token, List<BigDecimal> numericValues, List<String> alphabeticTokens) {
        StringBuilder digits  = new StringBuilder();
        StringBuilder letters = new StringBuilder();
        for (char c : token.toCharArray()) {
            if (Character.isDigit(c)) {
                if (letters.length() > 0) { addLetters(letters.toString(), alphabeticTokens); letters.setLength(0); }
                digits.append(c);
            } else if (Character.isLetter(c)) {
                if (digits.length() > 0) { numericValues.add(new BigDecimal(digits.toString())); digits.setLength(0); }
                letters.append(c);
            }
        }
        if (digits.length()  > 0) numericValues.add(new BigDecimal(digits.toString()));
        if (letters.length() > 0) addLetters(letters.toString(), alphabeticTokens);
    }

    private void addLetters(String letters, List<String> alphabeticTokens) {
        for (char c : letters.toCharArray()) alphabeticTokens.add(String.valueOf(Character.toUpperCase(c)));
    }

    private List<String> buildAlphabetsList(List<String> unique) {
        List<String> result = new ArrayList<>();
        for (String token : unique) {
            if (isPureAlpha(token)) {
                result.add(token.toUpperCase());
            } else if (!isPureNumber(token) && !isSpecialCharacter(token)) {
                for (char c : token.toCharArray()) {
                    if (Character.isLetter(c)) result.add(String.valueOf(Character.toUpperCase(c)));
                }
            }
        }
        return result;
    }

    private void extractAlphanumericLettersOnly(String token, List<String> alphabeticTokens) {
    for (char c : token.toCharArray()) {
        if (Character.isLetter(c)) {
            alphabeticTokens.add(String.valueOf(Character.toUpperCase(c)));
        }
    }
}

    private String formatNumber(BigDecimal n) {
        BigDecimal stripped = n.stripTrailingZeros();
        if (stripped.scale() < 0) stripped = stripped.setScale(0, RoundingMode.UNNECESSARY);
        return stripped.toPlainString();
    }
}
