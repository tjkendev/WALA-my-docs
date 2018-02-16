import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.ipa.callgraph.*;
import com.ibm.wala.cast.js.ipa.callgraph.correlations.extraction.CorrelatedPairExtractorFactory;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.loader.JavaScriptLoaderFactory;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.cast.tree.rewrite.CAstRewriterFactory;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;

public class Correlated {
  public static void main(String[] args) {
    String fileName = "correlation-tracking-example.js";
    try {
      File f = new FileProvider().getFile(fileName);
      SourceModule[] modules = new SourceModule[] {
        (SourceModule)  JSCallGraphUtil.getPrologueFile("prologue.js"),
          new SourceFileModule(f, fileName, null)
      };
      JavaScriptTranslatorFactory translatorFactory = new CAstRhinoTranslatorFactory();
      JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());

      // x[name] = y[name]となるペアをみつける
      CAstRewriterFactory preprocessor = new CorrelatedPairExtractorFactory(translatorFactory, modules);

      // ※JSCallGraphUtil.getPrologueFile()のストリームを一度使ったので、再生成
      SourceModule[] files = new SourceModule[] {
        (SourceModule)  JSCallGraphUtil.getPrologueFile("prologue.js"),
          new SourceFileModule(f, fileName, null)
      };

      // 生成したCorrelatedPairExtractorFactoryをpreprocessorとして設定
      JavaScriptLoaderFactory loaderFactory = JSCallGraphUtil.makeLoaders(preprocessor);
      AnalysisScope scope = JSCallGraphUtil.makeScope(files, loaderFactory, JavaScriptLoader.JS);
      IClassHierarchy cha = JSCallGraphUtil.makeHierarchy(scope, loaderFactory);

      JavaScriptEntryPoints roots = JSCallGraphUtil.makeScriptRoots(cha);

      JSAnalysisOptions options = JSCallGraphUtil.makeOptions(scope, cha, roots);

      IRFactory<IMethod> irFactory = AstIRFactory.makeDefaultFactory();
      AnalysisCache cache = (AnalysisCache) JSCallGraphUtil.makeCache(irFactory);

      JSCFABuilder builder = new JSZeroOrOneXCFABuilder(
          cha,
          options,
          cache,
          null,
          null,
          ZeroXInstanceKeys.ALLOCATIONS,
          true
          );
      // プロパティ名に紐づくコンテキスト設定
      ContextSelector contextSelector = new PropertyNameContextSelector(cache, 2, builder.getContextSelector());
      builder.setContextSelector(contextSelector);

      CallGraph callGraph = builder.makeCallGraph(options, null);
      PointerAnalysis pointerAnalysis = builder.getPointerAnalysis();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
