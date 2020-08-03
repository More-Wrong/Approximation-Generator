package com.wittsfamily.approximations.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wittsfamily.approximations.finder.FileRangeFinder;
import com.wittsfamily.approximations.rest.LookupFiles.LookupFile;

@RestController
@RequestMapping("/approx/")
public class LookupController {
    private final Map<String, FileRangeFinder> finders;

    public LookupController(LookupFiles ls) throws FileNotFoundException {
        finders = new HashMap<>(ls.getFiles().size());
        for (LookupFile f : ls.getFiles()) {
            finders.put(f.getName(), new FileRangeFinder(f.getLocation()));
        }
    }

    @RequestMapping("lookup/{value}")
    public Map<String, List<byte[]>> find(@PathVariable(value = "value") double value, @RequestParam(name = "range", required = false, defaultValue = "1") int range,
            @RequestParam(name = "target", required = false, defaultValue = "normal") String targetFile) throws IOException, ParseException {
        if (!finders.containsKey(targetFile)) {
            return Map.of();
        }
        try {
            return Map.of("values", finders.get(targetFile).find(value, range));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
