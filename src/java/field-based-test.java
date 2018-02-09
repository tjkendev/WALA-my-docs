import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.callgraph.fieldbased.OptimisticCallgraphBuilder;
import com.ibm.wala.cast.js.callgraph.fieldbased.flowgraph.vertices.ObjectVertex;
import com.ibm.wala.cast.js.ipa.callgraph.*;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.loader.JavaScriptLoaderFactory;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;

public class FieldBasedTest {
  public static void main(String[] args) {

    String fileName = "fieldbased-example.js";
    try {
      File f = new FileProvider().getFile(fileName);
      Module[] modules = new Module[] {
        JSCallGraphUtil.getPrologueFile("prologue.js"),
          new SourceFileModule(f, fileName, null)
      };
      JavaScriptTranslatorFactory translatorFactory = new CAstRhinoTranslatorFactory();
      JavaScriptLoaderFactory loaderFactory = new JavaScriptLoaderFactory(translatorFactory);
      AnalysisScope scope = JSCallGraphUtil.makeScope(modules, loaderFactory, JavaScriptLoader.JS);
      IClassHierarchy cha = JSCallGraphUtil.makeHierarchy(scope, loaderFactory);

      JavaScriptEntryPoints roots = JSCallGraphUtil.makeScriptRoots(cha);

      JSAnalysisOptions options = JSCallGraphUtil.makeOptions(scope, cha, roots);

      IRFactory<IMethod> irFactory = AstIRFactory.makeDefaultFactory();
      AnalysisCache cache = (AnalysisCache) JSCallGraphUtil.makeCache(irFactory);

      // Field-based Call Graph による解析
      OptimisticCallgraphBuilder builder = new OptimisticCallgraphBuilder(cha, options, cache, true);
      Pair<JSCallGraph, PointerAnalysis<ObjectVertex>> result = builder.buildCallGraph(roots, null);

      // Call Graph
      JSCallGraph callGraph = result.fst;
      // ポインタ解析結果
      PointerAnalysis<ObjectVertex> pointerAnalysis = result.snd;
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
