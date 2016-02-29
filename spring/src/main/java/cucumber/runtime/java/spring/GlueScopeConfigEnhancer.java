package cucumber.runtime.java.spring;

import java.io.IOException;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.test.context.ContextConfiguration;

public class GlueScopeConfigEnhancer implements Opcodes{
	
	private class ASMClassLoader extends ClassLoader{
		public Class defineClass(String name, byte[] content){
			return defineClass(name, content, 0, content.length);
		}
	}
	
	public Class enhance(Class class1) {
		try {
			String className = Type.getInternalName(class1);

			ClassWriter cw = new ClassWriter(0);
			
			ContextConfigurationClassVisitor contextCv = new ContextConfigurationClassVisitor(cw);
			
			ClassReader cr = new ClassReader(className);
			cr.accept(contextCv, 0);
			
			return new ASMClassLoader().defineClass(className.replace('/', '.'), cw.toByteArray());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public class ContextConfigurationClassVisitor extends ClassVisitor{
		public ContextConfigurationClassVisitor(ClassVisitor cv){
			super(ASM5, cv);
		}
		
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if(Type.getDescriptor(ContextConfiguration.class).equals(desc)){
				return new ContextConfigurationAnnotationVisitor(super.visitAnnotation(desc, visible));
			}
			return super.visitAnnotation(desc, visible);
		}
		
	}
	
	public class ContextConfigurationAnnotationVisitor extends AnnotationVisitor {
		
		public ContextConfigurationAnnotationVisitor(AnnotationVisitor av){
			super(ASM5, av);
		}
		
		@Override
		public AnnotationVisitor visitArray(String name) {
			if("classes".equals(name)){
				return new ContextConfigurationAnnotationArrayVisitor(super.visitArray(name));
			}
			return super.visitArray(name);
		}
	}
	
	public class ContextConfigurationAnnotationArrayVisitor extends AnnotationVisitor {
		
		public ContextConfigurationAnnotationArrayVisitor(AnnotationVisitor av){
			super(ASM5, av);
		}

		@Override
		public void visit(String name, Object value) {
			super.visit(name, value);
		}
		
		@Override
		public void visitEnd() {
			visit(null, Type.getType(GlueScopeConfig.class));
			super.visitEnd();
		}
	}

}
