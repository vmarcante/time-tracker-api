package com.vmarcante.time_tracker.core.shared.vo;

import java.util.Set;
import java.util.regex.Pattern;

import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

public class PhoneValidator {

    private static final Pattern REPEATED_DIGITS_PATTERN = Pattern.compile("(\\d)\\1+");

    private static final Set<Integer> VALID_DDDS = Set.of(
            11, 12, 13, 14, 15, 16, 17, 18, 19, // São Paulo
            21, 22, 24, // Rio de Janeiro
            27, 28, // Espírito Santo
            31, 32, 33, 34, 35, 37, 38, // Minas Gerais
            41, 42, 43, 44, 45, 46, // Paraná
            47, 48, 49, // Santa Catarina
            51, 53, 54, 55, // Rio Grande do Sul
            61, // Distrito Federal
            62, 64, // Goiás
            63, // Tocantins
            65, 66, // Mato Grosso
            67, // Mato Grosso do Sul
            68, // Acre
            69, // Rondônia
            71, 73, 74, 75, 77, // Bahia
            79, // Sergipe
            81, 87, // Pernambuco
            82, // Alagoas
            83, // Paraíba
            84, // Rio Grande do Norte
            85, 88, // Ceará
            86, 89, // Piauí
            91, 93, 94, // Pará
            92, 97, // Amazonas
            95, // Roraima
            96, // Amapá
            98, 99 // Maranhão
    );

    private PhoneValidator() {
    }

    static boolean isValid(String phone) {
        if (!StringValidationUtils.containsContent(phone)) {
            return true; // Null or empty is valid (optional field)
        }

        String cleanNumber = cleanPhoneNumber(phone);

        // Telefone fixo: (XX) XXXX-XXXX = 10 dígitos
        // Celular: (XX) 9XXXX-XXXX = 11 dígitos
        if (cleanNumber.length() != 10 && cleanNumber.length() != 11) {
            return false;
        }

        // Validar DDD brasileiro válido
        int ddd = Integer.parseInt(cleanNumber.substring(0, 2));
        if (!VALID_DDDS.contains(ddd)) {
            return false;
        }

        // Se for celular (11 dígitos), o terceiro dígito deve ser 9
        if (cleanNumber.length() == 11) {
            char thirdDigit = cleanNumber.charAt(2);
            if (thirdDigit != '9') {
                return false;
            }
        }

        // Validar se não são todos os dígitos iguais
        if (REPEATED_DIGITS_PATTERN.matcher(cleanNumber).matches()) {
            return false;
        }

        return true;
    }

    static String clean(String phone) {
        if (phone == null) {
            return null;
        }
        return cleanPhoneNumber(phone);
    }

    private static String cleanPhoneNumber(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }
}
