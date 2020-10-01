package io.github.dawncraft.qingchenw.random.utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机数生成引擎
 * <p>
 * Created on 2020/7/9
 *
 * @param <T> 待随机的集合中的元素的类型
 * @author QingChenW
 */
public class RandomEngine<T>
{
    // 随机数
    private Random rand;
    // js脚本引擎
    private Context javaScriptContext;
    // js脚本
    private String scriptCode;
    
    // 元素列表
    private T[] elements;
    private int range = 0;
    
    public RandomEngine()
    {
        rand = new Random();
    }
    
    public RandomEngine(long seed)
    {
        this();
        rand.setSeed(seed);
    }
    
    public void initJSEngine()
    {
        javaScriptContext = Context.enter();
        javaScriptContext.setOptimizationLevel(-1);
    }
    
    public void setScript(String code)
    {
        scriptCode = code;
    }
    
    public void setElementList(List<T> list)
    {
        if (list != null && !list.isEmpty())
        {
            range = list.size();
            elements = (T[]) list.toArray();
        }
    }
    
    public boolean hasElement()
    {
        return range > 0;
    }
    
    public T generate() throws InvalidCodeException
    {
        int result = rand.nextInt(range);
        result = runScript(result);
        return elements[result];
    }

    public int runScript(int oldResult) throws InvalidCodeException
    {
        if (javaScriptContext != null)
        {
            Scriptable scope = javaScriptContext.initStandardObjects();
            javaScriptContext.evaluateString(scope, scriptCode, null, 1, null);
            Object jsObj = scope.get("generate" , scope);
            if (jsObj instanceof Function)
            {
                Object[] args = {elements, range, oldResult};
                Function function = (Function) jsObj;
                Object returnValue = function.call(javaScriptContext, scope, scope, args);
                int newResult = (int) Context.toNumber(returnValue);
                if (newResult >= 0 && newResult < range)
                {
                    return newResult;
                }
            }
            throw new InvalidCodeException("Invalid JavaScript code", oldResult);
        }
        return oldResult;
    }
    
    public void release()
    {
        if (javaScriptContext != null)
        {
            Context.exit();
            javaScriptContext = null;
        }
    }
    
    public static class InvalidCodeException extends Exception
    {
        private int oldResult;
        
        public InvalidCodeException(String message, int result)
        {
            super(message);
            oldResult = result;
        }
        
        public int getResult()
        {
            return oldResult;
        }
    }
}
