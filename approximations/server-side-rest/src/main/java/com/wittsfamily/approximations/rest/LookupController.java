package com.wittsfamily.approximations.rest;

import org.springframework.web.bind.annotation.RestController;

import com.wittsfamily.approximations.finder.FileRangeFinder;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/approx/")
public class LookupController {
	private final FileRangeFinder f;

	public LookupController(FileRangeFinder f) {
		this.f = f;
	}

	@RequestMapping("lookup/{value}")
	public Map<String, List<byte[]>> find(@PathVariable(value = "value") double value,
			@RequestParam(name = "range", required = false, defaultValue = "1") int range)
			throws IOException, ParseException {
		try {
			return Map.of("values", f.find(value, range));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
