package org.ayou.search.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ayou.search.domain.DownFileEntity;
import org.ayou.search.util.Search;
import org.ayou.search.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
public class FileAction {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private Environment env;

	@PostMapping("/search")
	public Object getFiles(DownFileEntity dEntity) {
		String filePath = env.getProperty("File.Path");
		String searchThreads = env.getProperty("Search.Threads");
		@SuppressWarnings("unused")
		int threads = Runtime.getRuntime().availableProcessors();
		if (StringUtils.hasText(searchThreads)) {
			threads = Integer.parseInt(searchThreads);
		}
		String path;
		if (!StringUtils.hasText(filePath) && !StringUtils.hasText(dEntity.getPath())) {
			return "-1";// 没有指定-DFile.Path
		} else if (StringUtils.hasText(dEntity.getPath())) {
			path = dEntity.getPath() + "/" + dEntity.getType() + "/" + dEntity.getTime();
		} else {
			path = filePath + "/" + dEntity.getType() + "/" + dEntity.getTime();
		}
		File folder = new File(path);
		if (!folder.exists()) {
			return "0";// 没有路径
		}
		File[] result = Search.getAllFile(folder);// 调用方法获得文件数组
		//List<String> list = Search.search2(result, dEntity.getKey(), threads);
		List<String> list = Search.search4Java8(result, dEntity.getKey());
		if (list.isEmpty()) {
			return "-2";// 没有搜索到相关报文！
		}
		return list;
	}

	@PostMapping("/zip")
	public Object getFiles(HttpServletRequest request, DownFileEntity dEntity) {
		String filePath = env.getProperty("File.Path");
		String path;
		if (!StringUtils.hasText(filePath) && !StringUtils.hasText(dEntity.getPath())) {
			return "-1";// 没有指定-DFile.Path
		} else if (StringUtils.hasText(dEntity.getPath())) {
			path = dEntity.getPath() + "/" + dEntity.getType() + "/" + dEntity.getTime();
		} else {
			path = filePath + "/" + dEntity.getType() + "/" + dEntity.getTime();
		}
		if (dEntity.getList().isEmpty()) {
			return "-2";// 没有要下载的
		}
		String zip = path + "/" + dEntity.getType() + "-" + dEntity.getTime() + "-" + dEntity.getKey() + ".zip";
		List<String> fs = new ArrayList<>();

		dEntity.getList().forEach(x -> {
			if (StringUtils.hasText(x)) {
				fs.add(x.replace("\"", "").replaceAll("\\[", "").replaceAll("\\]", ""));
			}
		});

		// 压缩
		try {
			ZipUtils.doCompress(fs, new File(zip));
		} catch (Exception e) {
			e.printStackTrace();
			return "0";// 后台异常
		}
		return zip;
	}

	@GetMapping("/down")
	public Object down(HttpServletRequest request, String file) {
		InputStream is = null;
		byte[] body = null;
		HttpHeaders headers = new HttpHeaders();
		try {
			File f = new File(file);
			is = new FileInputStream(f);
			body = new byte[is.available()];
			is.read(body);
			headers.add("Content-Disposition", "attchement;filename=" + f.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return "0";// 后台异常
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "0";// 后台异常
			}
		}
		//ResponseEntity<byte[]>(body, headers, HttpStatus.OK)
		ResponseEntity<byte[]> entity = new ResponseEntity<>(body, headers, HttpStatus.OK);
		return entity;
	}

	@PostMapping("/server")
	public String serverT(String hello) {
		return "I get a Parm is " + hello;
	}

	@GetMapping("/hello")
	public String hello(String hello) {
		return "I get a Parm is " + hello;
	}
	
	@GetMapping("/getMac")
	public String getMac(HttpServletRequest request, HttpSession session) {
		String macAddress = (String) request.getParameter("MAC_ADDRESS");
		System.out.println(macAddress);
		return macAddress;
	}
}
