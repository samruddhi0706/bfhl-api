package com.bajaj.bfhl.service;

import com.bajaj.bfhl.dto.BfhlRequest;
import com.bajaj.bfhl.dto.BfhlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BfhlServiceImplTest {

    private BfhlServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BfhlServiceImpl();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private BfhlRequest request(Object... elements) {
        BfhlRequest req = new BfhlRequest();
        req.setData(Arrays.asList(elements));
        return req;
    }

    // ── Example 1: basic input ────────────────────────────────────────────────

    @Test
    @DisplayName("Example 1 – basic mixed input")
    void example1() {
        BfhlRequest req = request("A", "1", "22", "$", "B", "7");
        BfhlResponse res = service.process(req, "REQ-1001");

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getRequestId()).isEqualTo("REQ-1001");
        assertThat(res.getOddNumbers()).containsExactlyInAnyOrder("1", "7");
        assertThat(res.getEvenNumbers()).containsExactlyInAnyOrder("22");
        assertThat(res.getAlphabets()).containsExactlyInAnyOrder("A", "B");
        assertThat(res.getSpecialCharacters()).containsExactlyInAnyOrder("$");
        assertThat(res.getSum()).isEqualTo("30");
        assertThat(res.getLargestNumber()).isEqualTo("22");
        assertThat(res.getSmallestNumber()).isEqualTo("1");
        assertThat(res.getAlphabetCount()).isEqualTo(2);
        assertThat(res.getNumberCount()).isEqualTo(3);
        assertThat(res.getSpecialCharacterCount()).isEqualTo(1);
        assertThat(res.getContainsDuplicates()).isFalse();
    }

    // ── Example 2: alphanumeric tokens ────────────────────────────────────────

    @Test
    @DisplayName("Example 2 – alphanumeric tokens")
    void example2() {
        BfhlRequest req = request("A1B2", "100", "#", "Test123", "Z", "55");
        BfhlResponse res = service.process(req, "REQ-1002");

        assertThat(res.getSpecialCharacters()).containsExactly("#");
        assertThat(res.getSum()).isEqualTo("155");
        assertThat(res.getLargestNumber()).isEqualTo("100");
        assertThat(res.getSmallestNumber()).isEqualTo("55");
        assertThat(res.getContainsDuplicates()).isFalse();
    }

    // ── Example 3: duplicates, nulls, blanks ──────────────────────────────────

    @Test
    @DisplayName("Example 3 – duplicates, nulls, empty strings")
    void example3() {
        BfhlRequest req = request("10", "10", "A", "A", "", null, "&", "5");
        BfhlResponse res = service.process(req, "REQ-1003");

        assertThat(res.getContainsDuplicates()).isTrue();
        assertThat(res.getOddNumbers()).containsExactly("5");
        assertThat(res.getEvenNumbers()).containsExactly("10");
        assertThat(res.getAlphabets()).containsExactly("A");
        assertThat(res.getSpecialCharacters()).containsExactly("&");
        assertThat(res.getSum()).isEqualTo("15");
        assertThat(res.getUniqueElementCount()).isEqualTo(4);
        assertThat(res.getSummary().getTotalElementsReceived()).isEqualTo(8);
        assertThat(res.getSummary().getInvalidElementsIgnored()).isEqualTo(2); // "" and null
    }

    // ── Example 4: negatives and decimals ─────────────────────────────────────

    @Test
    @DisplayName("Example 4 – negative and decimal numbers")
    void example4() {
        BfhlRequest req = request("-10", "25.5", "-100.75", "B", "@", "5", "A9");
        BfhlResponse res = service.process(req, "REQ-1004");

        assertThat(res.getOddNumbers()).containsExactly("5");
        assertThat(res.getEvenNumbers()).containsExactly("-10");
        assertThat(res.getSum()).isEqualTo("-80.25");
        assertThat(res.getLargestNumber()).isEqualTo("25.5");
        assertThat(res.getSmallestNumber()).isEqualTo("-100.75");
        assertThat(res.getContainsDuplicates()).isFalse();
    }

    // ── Example 5: comprehensive ──────────────────────────────────────────────

    @Test
    @DisplayName("Example 5 – comprehensive mixed input with vowels and consonants")
    void example5() {
        BfhlRequest req = request(
                "ABC", "123", "A1B2", "$", "%", "-50", "0", "xyz", "", null, "999", "Test99", "&");
        BfhlResponse res = service.process(req, "REQ-1005");

        assertThat(res.getOddNumbers()).containsExactlyInAnyOrder("123", "999");
        assertThat(res.getEvenNumbers()).containsExactlyInAnyOrder("-50", "0");
        assertThat(res.getSpecialCharacters()).containsExactlyInAnyOrder("$", "%", "&");
        assertThat(res.getLargestNumber()).isEqualTo("999");
        assertThat(res.getSmallestNumber()).isEqualTo("-50");
        assertThat(res.getVowelCount()).isGreaterThan(0);
        assertThat(res.getConsonantCount()).isGreaterThan(0);
        assertThat(res.getSummary().getTotalElementsReceived()).isEqualTo(13);
        assertThat(res.getSummary().getInvalidElementsIgnored()).isEqualTo(2); // "" and null
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Empty data list")
    void emptyData() {
        BfhlRequest req = new BfhlRequest();
        req.setData(Collections.emptyList());
        BfhlResponse res = service.process(req, "REQ-EMPTY");

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getOddNumbers()).isEmpty();
        assertThat(res.getEvenNumbers()).isEmpty();
        assertThat(res.getAlphabets()).isEmpty();
        assertThat(res.getSpecialCharacters()).isEmpty();
        assertThat(res.getSum()).isEqualTo("0");
        assertThat(res.getNumberCount()).isEqualTo(0);
        assertThat(res.getAlphabetCount()).isEqualTo(0);
        assertThat(res.getSpecialCharacterCount()).isEqualTo(0);
        assertThat(res.getContainsDuplicates()).isFalse();
    }

    @Test
    @DisplayName("Only null and blank values")
    void onlyNullsAndBlanks() {
        BfhlRequest req = request(null, "", "   ");
        BfhlResponse res = service.process(req, "REQ-BLANKS");

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getSummary().getTotalElementsReceived()).isEqualTo(3);
        assertThat(res.getSummary().getValidElementsProcessed()).isEqualTo(0);
        assertThat(res.getSummary().getInvalidElementsIgnored()).isEqualTo(3);
        assertThat(res.getContainsDuplicates()).isFalse();
    }

    @Test
    @DisplayName("Single special character")
    void singleSpecialCharacter() {
        BfhlRequest req = request("@");
        BfhlResponse res = service.process(req, "REQ-SP");

        assertThat(res.getSpecialCharacters()).containsExactly("@");
        assertThat(res.getAlphabets()).isEmpty();
        assertThat(res.getOddNumbers()).isEmpty();
        assertThat(res.getEvenNumbers()).isEmpty();
    }

    @Test
    @DisplayName("All duplicate elements")
    void allDuplicates() {
        BfhlRequest req = request("5", "5", "5");
        BfhlResponse res = service.process(req, "REQ-DUP");

        assertThat(res.getContainsDuplicates()).isTrue();
        assertThat(res.getUniqueElementCount()).isEqualTo(1);
        assertThat(res.getOddNumbers()).containsExactly("5");
        assertThat(res.getSum()).isEqualTo("5");
    }

    @Test
    @DisplayName("Sorted numbers are in ascending order")
    void sortedNumbers() {
        BfhlRequest req = request("100", "-50", "0", "25");
        BfhlResponse res = service.process(req, "REQ-SORT");

        assertThat(res.getSortedNumbers()).containsExactly("-50", "0", "25", "100");
    }

    @Test
    @DisplayName("Alphabet frequency is populated correctly")
    void alphabetFrequency() {
        BfhlRequest req = request("A", "B", "A");
        BfhlResponse res = service.process(req, "REQ-FREQ");

        assertThat(res.getContainsDuplicates()).isTrue();
        // After de-dup, only one "A" and one "B"
        assertThat(res.getAlphabetFrequency()).containsEntry("A", 1);
        assertThat(res.getAlphabetFrequency()).containsEntry("B", 1);
    }

    @Test
    @DisplayName("Longest and shortest alphabetic value")
    void longestAndShortest() {
        BfhlRequest req = request("A", "HELLO", "HI");
        BfhlResponse res = service.process(req, "REQ-LEN");

        assertThat(res.getLongestAlphabeticValue()).isEqualTo("HELLO");
        assertThat(res.getShortestAlphabeticValue()).isEqualTo("A");
    }

    @Test
    @DisplayName("Vowel and consonant counts are correct")
    void vowelAndConsonantCounts() {
        // "AEIOU" has 5 vowels, 0 consonants; "BCF" has 0 vowels, 3 consonants
        BfhlRequest req = request("AEIOU", "BCF");
        BfhlResponse res = service.process(req, "REQ-VOWEL");

        assertThat(res.getVowelCount()).isEqualTo(5);
        assertThat(res.getConsonantCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Zero is treated as an even number")
    void zeroIsEven() {
        BfhlRequest req = request("0");
        BfhlResponse res = service.process(req, "REQ-ZERO");

        assertThat(res.getEvenNumbers()).containsExactly("0");
        assertThat(res.getOddNumbers()).isEmpty();
    }

    @Test
    @DisplayName("Decimal numbers are excluded from odd/even buckets")
    void decimalNotOddOrEven() {
        BfhlRequest req = request("2.5", "3.0");
        BfhlResponse res = service.process(req, "REQ-DEC");

        // 2.5 → decimal, excluded; 3.0 → integer value 3, odd
        assertThat(res.getOddNumbers()).containsExactly("3");
        assertThat(res.getEvenNumbers()).isEmpty();
    }

    @Test
    @DisplayName("Processing time is returned in milliseconds")
    void processingTimeReturned() {
        BfhlRequest req = request("X");
        BfhlResponse res = service.process(req, "REQ-TIME");

        assertThat(res.getProcessingTimeMs()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("Request ID is echoed back correctly")
    void requestIdEchoed() {
        BfhlRequest req = request("1");
        BfhlResponse res = service.process(req, "MY-CUSTOM-ID");

        assertThat(res.getRequestId()).isEqualTo("MY-CUSTOM-ID");
    }
}
