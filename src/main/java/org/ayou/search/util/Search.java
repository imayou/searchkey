package org.ayou.search.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Search {
	static int countFiles = 0;// 声明统计文件个数的变量
	static int countFolders = 0;// 声明统计文件夹的变量

	/**
	 * 获取所以文件
	 * 
	 * @param folder
	 * @return
	 */
	public static File[] getAllFile(File folder) {// 递归查找包含关键字的文件
		File[] subFolders = folder.listFiles(x -> x.getAbsolutePath().endsWith(".xml"));

		/*
		 * new FileFilter() {// 运用内部匿名类获得文件
		 * 
		 * @Override public boolean accept(File pathname) {//
		 * 实现FileFilter类的accept方法 if (pathname.isFile()) {// 如果是文件 countFiles++;
		 * } else {// 如果是目录 countFolders++; } if
		 * (!pathname.getAbsolutePath().endsWith(".xml")) { return false; }
		 * return true; } }
		 */

		List<File> result = new ArrayList<File>();// 声明一个集合
		for (int i = 0; i < subFolders.length; i++) {// 循环显示文件夹或文件
			if (subFolders[i].isFile()) {// 如果是文件则将文件添加到结果列表中
				result.add(subFolders[i]);
			} else {// 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
				File[] foldResult = getAllFile(subFolders[i]);
				for (int j = 0; j < foldResult.length; j++) {// 循环显示文件
					result.add(foldResult[j]);// 文件保存到集合中
				}
			}
		}

		File files[] = new File[result.size()];// 声明文件数组，长度为集合的长度
		result.toArray(files);// 集合数组化
		return files;
	}

	/**
	 * 搜索
	 * 
	 * @param files
	 * @param key
	 * @return
	 */
	public static List<String> searchNoT(File[] files, String key) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			String str = FileUtil.readStringByNIO(files[i].getAbsolutePath(), 16);// 16k的tmp
			// String str = FileUtil.readerFile(files[index].getAbsolutePath());
			if (StringUtils.ishave(str, key)) {
				list.add(files[i].getAbsolutePath());
				// 判断是否有响应或请求报文
				String res = null;
				if (files[i].getAbsolutePath().contains("request")) {
					res = files[i].getAbsolutePath().replace("request", "response");
				} else if (files[i].getAbsolutePath().contains("response")) {
					res = files[i].getAbsolutePath().replace("response", "request");
				}
				File f = new File(res);
				if (f.exists()) {
					list.add(res);
				}
			}
		}

		// 去重
		HashSet<String> h = new HashSet<String>(list);
		list.clear();
		list.addAll(h);
		return list;
	}

	/**
	 * 搜索
	 * 
	 * @param files
	 * @param key
	 * @return
	 */
	public static List<String> search(File[] files, String key) {
		List<String> list = Collections.synchronizedList(new ArrayList<String>());
		List<Runnable> tasks = new ArrayList<>(files.length);
		for (int i = 0; i < files.length; i++) {
			final int index = i;
			tasks.add(() -> {
				String str = FileUtil.readStringByNIO(files[index].getAbsolutePath(), 16);// 16k的tmp
				if (StringUtils.ishave(str, key)) {
					list.add(files[index].getAbsolutePath());
					// 判断是否有响应或请求报文
					String res = null;
					if (files[index].getAbsolutePath().contains("request")) {
						res = files[index].getAbsolutePath().replace("request", "response");
					} else if (files[index].getAbsolutePath().contains("response")) {
						res = files[index].getAbsolutePath().replace("response", "request");
					}
					File f = new File(res);
					if (f.exists()) {
						list.add(res);
					}
				}
			});
		}
		AsyncTaskUtil.executeAsyncTasks(tasks, true);
		// 去重
		HashSet<String> h = new HashSet<String>(list);
		list.clear();
		list.addAll(h);
		return list;
	}

	/**
	 * 搜索
	 * 
	 * @param files
	 * @param key
	 * @param threads
	 * @return
	 */
	public static List<String> search2(File[] files, String key, int threads) {
		//List<String> list = Collections.synchronizedList(new ArrayList<String>());
		Set<String> list = Collections.synchronizedSet(new TreeSet<>());
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threads);
		Semaphore semaphore = new Semaphore(0,true);
		try {
			for (int i = 0; i < files.length; i++) {
				final int index = i;
				fixedThreadPool.execute(()->{
					try {
						//boolean has = Files.lines(Paths.get(files[index].getAbsolutePath()), Charset.forName(FileUtil.codeString2(files[index].getAbsolutePath()))).parallel().anyMatch(str -> str.contains(key));
						String str = FileUtil.readStringByNIO(files[index].getAbsolutePath(), 16);// 16k的tmp
						if (StringUtils.ishave(str, key)) {
							list.add(files[index].getAbsolutePath());
							// 判断是否有响应或请求报文
							String res = null;
							if (files[index].getAbsolutePath().contains("request")) {
								res = files[index].getAbsolutePath().replace("request", "response");
							} else if (files[index].getAbsolutePath().contains("response")) {
								res = files[index].getAbsolutePath().replace("response", "request");
							}
							if (FileUtil.exists(res)) {
								list.add(res);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						semaphore.release();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				semaphore.acquire(files.length);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			fixedThreadPool.shutdown();
		}
		return new ArrayList<String>(list);
	}
	
	/**
	 * java8的并行
	 * @param files
	 * @param key
	 * @return
	 */
	public static List<String> search4Java8(File[] files, String key) {
		//List<String> list = Collections.synchronizedList(new ArrayList<String>());
		//List<String> list = Collections.synchronizedList(new LinkedList<String>());
		//Set<String> list = Collections.synchronizedSet(new HashSet<>());
		Set<String> list = Collections.synchronizedSet(new TreeSet<>());
		List<String> fs = Stream.of(files).parallel().map(f -> f.getAbsolutePath()).collect(Collectors.toList());
		List<String> have = fs.parallelStream().filter(path -> {
			String str = FileUtil.readStringByNIO(path, 16);
			return StringUtils.ishave(str, key);
//			try {
//				return Files.lines(Paths.get(path), Charset.forName(FileUtil.codeString2(path))).parallel().anyMatch(str -> str.contains(key));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return false;
		}).collect(Collectors.toList());
		have.parallelStream().filter(path -> {
			list.add(path);
			String res = null;
			if (path.contains("request")) {
				res = path.replace("request", "response");
			} else if (path.contains("response")) {
				res = path.replace("response", "request");
			}else{
				return false;
			}
			if (FileUtil.exists(res)) {
				list.add(res);
			}
			return false;
		}).count();
		return list.stream().collect(Collectors.toList());
		//return list.parallelStream().collect(Collectors.toList());
	}
	
	/**
	 * java8并发-线程池
	 * @param files
	 * @param key
	 * @return
	 */
	public static List<String> s48(File[] files, String key) {
		List<CompletableFuture<String>> futures = Stream.of(files).parallel().map(file -> CompletableFuture.supplyAsync(() -> {
			String str = FileUtil.readStringByNIO(file.getAbsolutePath(), 6);//6k
			if (StringUtils.ishave(str, key)) {
				return file.getAbsolutePath();
			} else {
				return null;
			}
		},AsyncTaskUtil2.getExecutor(10))).collect(Collectors.toList());//AsyncTaskUtil2.getExecutor(10)
		return futures.parallelStream().map(CompletableFuture::join).distinct().filter(x->{return x!=null;}).collect(Collectors.toList());
	}

	public static void main(String[] args) {
		//File folder = new File("D:\\Interface\\makePremium\\20170807");// 默认目录-机械
		File folder = new File("C:\\developer\\tmp\\20170807");// 默认目录-固态
		File[] result = getAllFile(folder);
		int t = (int) (Runtime.getRuntime().availableProcessors());
		/*
		 * for (int i = 0; i < result.length; i++) { File file = result[i];
		 * System.err.println(file.getAbsolutePath());// 显示文件绝对路径 }
		 */
//		for(;;){
//			long startnoT = System.currentTimeMillis();
//			List<String> nt = searchNoT(result, "48364037674399659");
//			long endnoT = System.currentTimeMillis();
//			System.err.println("不用线程搜索了" + result.length + "个文件搜到"+nt.size()+"个文件，用时：" + (endnoT - startnoT));
//			//System.out.println(nt);
//		}
		
//		for(;;){
//			long startnio = System.currentTimeMillis();
//			List<String> xx = search2(result, "48364037674399659",t);
//			long endnio = System.currentTimeMillis();
//			System.err.println("用"+Runtime.getRuntime().availableProcessors()+ "个核心"+t + "个线程和java8流搜索了" + result.length + "个文件搜到"+xx.size()+"个文件，用时：" + (endnio - startnio));
//			//System.out.println(xx);
//		}
		
//		for(;;){
//			long start = System.currentTimeMillis();
//			List<String> ls = search(result, "48364037674399659");
//			long end = System.currentTimeMillis();
//			System.err.println("并发搜索了" + result.length + "个文件搜到"+ls.size()+"个文件，用时：" + (end - start));
//			//System.out.println(ls);
//		}
		
//5260a8f540ab463cad4b63064a7657c3		
		for(;;){
			long startnio = System.currentTimeMillis();
			List<String> xx = search4Java8(result, "48364037674399659");
			long endnio = System.currentTimeMillis();
			System.err.println("用"+Runtime.getRuntime().availableProcessors()+ "个核心"+t + "个线程和java8流搜索了" + result.length + "个文件搜到"+xx.size()+"个文件，用时：" + (endnio - startnio));
			//System.out.println(xx);
		}
		
//		for(;;){
//			long startnio = System.currentTimeMillis();
//			List<String> xx = s48(result, "48364037674399659");
//			long endnio = System.currentTimeMillis();
//			System.err.println("用"+Runtime.getRuntime().availableProcessors()+ "个核心java8新特性搜索了" + result.length + "个文件搜到"+xx.size()+"个文件，用时：" + (endnio - startnio));
//			System.out.println(xx);
//		}
		
		/*
		 * try { ZipUtils.doCompress(ls, new File("20170807.zip")); } catch
		 * (Exception e) { e.printStackTrace(); }
		 */
	}
}
