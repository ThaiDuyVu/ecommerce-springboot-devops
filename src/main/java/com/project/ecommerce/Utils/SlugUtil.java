package com.project.ecommerce.Utils;

import java.text.Normalizer;

public class SlugUtil {

    public static String toSlug(String input) {

        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        return slug;
    }

}